package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GryffsBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gryff's Boon attaches it and grants +1/+0 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GryffsBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gryff's Boon")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        // Grizzly Bears 2/2 +1/+0 = 3/2 with flying
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns Boon attached to target creature")
    void graveyardAbilityReturnsAttached() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        GryffsBoon boon = new GryffsBoon();
        harness.setGraveyard(player1, List.of(boon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gryff's Boon"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gryff's Boon")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability fizzles if target leaves; Boon stays in graveyard")
    void illegalTargetFizzlesAndBoonStays() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GryffsBoon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gryff's Boon"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gryff's Boon"));
    }

    @Test
    @DisplayName("Graveyard ability requires a creature target")
    void requiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setGraveyard(player1, List.of(new GryffsBoon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability can only be activated at sorcery speed")
    void onlyAtSorcerySpeed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GryffsBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attach from graveyard to an opponent's creature")
    void canAttachToOpponentCreature() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.setGraveyard(player1, List.of(new GryffsBoon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gryff's Boon")
                        && p.getAttachedTo().equals(giant.getId()));
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isTrue();
    }
}
