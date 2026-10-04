package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuardChange;
import com.github.laxika.magicalvibes.cards.r.RedtoothVanguard;
import com.github.laxika.magicalvibes.cards.t.TorchTheTower;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerociousWerefox.class, GuardChange.class, GrizzlyBears.class,
        RedtoothVanguard.class, TorchTheTower.class})
class FerociousWerefoxTest extends BaseCardTest {

    @Test
    void adventureCreatesMonsterRoleAttachedToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FerociousWerefox card = new FerociousWerefox();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FerociousWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FerociousWerefox card = new FerociousWerefox();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FerociousWerefox);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        FerociousWerefox card = new FerociousWerefox();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureWithRemovedTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        FerociousWerefox card = new FerociousWerefox();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());

        harness.setHand(player2, List.of(new TorchTheTower()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Monster")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void secondMonsterRoleReplacesTheOlderRoleWithoutStackingBonuses() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RedtoothVanguard());
        harness.setHand(player1, List.of(new FerociousWerefox(), new FerociousWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent olderRole = findPermanent(player1, "Monster");

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monster")).hasSize(1);
        Permanent newerRole = findPermanent(player1, "Monster");
        assertThat(newerRole.getId()).isNotEqualTo(olderRole.getId());
        assertThat(newerRole.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void creatureCastDirectlyFromHandDealsTrampleDamage() {
        harness.castFromHand(player1, new FerociousWerefox(), "{3}{G}");
        harness.passBothPriorities();
        Permanent werefox = findPermanent(player1, "Ferocious Werefox");
        werefox.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RedtoothVanguard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Ferocious Werefox");
        harness.assertInGraveyard(player2, "Redtooth Vanguard");
    }
}
