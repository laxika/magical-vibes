package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.p.PheresBandTromper;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.u.UnravelTheAether;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaisedByWolves.class, PheresBandTromper.class, NyxbornWolf.class,
        SpringleafDrum.class, UnravelTheAether.class})
class RaisedByWolvesTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    void enteringBattlefieldCreatesTwoWolvesAndBoostsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        harness.setHand(player1, List.of(new RaisedByWolves()));
        addCastingMana();

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WOLF))
                .toList();

        assertThat(wolves).hasSize(2).allSatisfy(wolf -> {
            assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(wolf.getCard().getPower()).isEqualTo(2);
            assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    void boostTracksWolvesControlled() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RaisedByWolves());
        aura.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new NyxbornWolf());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new RaisedByWolves()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bauble.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantingOpponentsCreatureCountsOnlyAurasControllersWolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PheresBandTromper());
        harness.addToBattlefield(player1, new NyxbornWolf());
        harness.addToBattlefield(player2, new NyxbornWolf());
        harness.addToBattlefield(player2, new NyxbornWolf());
        harness.setHand(player1, List.of(new RaisedByWolves()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void enchantedWolfCountsItselfAndBoostDecreasesWhenAnotherWolfLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent otherWolf = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RaisedByWolves());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, otherWolf.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void tokenTriggerStillResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PheresBandTromper());
        harness.setHand(player1, List.of(new RaisedByWolves(), new UnravelTheAether()));
        addCastingMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Raised by Wolves");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Raised by Wolves");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void illegalAuraTargetPreventsAuraEnteringAndCreatingTokens() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        harness.setHand(player1, List.of(new RaisedByWolves(), new UnravelTheAether()));
        addCastingMana();
        harness.castEnchantment(player1, 0, creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Raised by Wolves");
        harness.assertNotOnBattlefield(player1, "Raised by Wolves");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
