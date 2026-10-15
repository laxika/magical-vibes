package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraFinesse;
import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.cards.u.UndiscoveredParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunClasp.class, LongbowArcher.class, UndiscoveredParadise.class, AuraFinesse.class})
class SunClaspTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sun Clasp attaches it and gives the creature +1/+3")
    void resolvingAttachesAndBoosts() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SunClasp
                        && p.isAttached()
                        && p.getAttachedTo().equals(archer.getId()));
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sun Clasp does not boost other creatures")
    void doesNotBoostOtherCreatures() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());
        Permanent otherArcher = addCreatureReady(player1, new LongbowArcher());

        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(archer.getId());

        assertThat(gqs.getEffectivePower(gd, otherArcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherArcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating {W} returns enchanted creature to hand; Aura dies as orphaned")
    void activatedAbilityBouncesEnchantedCreature() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(archer.getId());

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof LongbowArcher || p.getCard() instanceof SunClasp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
    }

    @Test
    @DisplayName("Sun Clasp fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(archer);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SunClasp);
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new LongbowArcher());
        harness.addToBattlefield(player1, new UndiscoveredParadise());
        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent paradise = findPermanent(player1, "Undiscovered Paradise");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, paradise.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returning an opponent's enchanted creature puts it in its owner's hand")
    void returnsOpponentCreatureToItsOwnersHand() {
        LongbowArcher archerCard = new LongbowArcher();
        archerCard.setOwnerId(player2.getId());
        Permanent archer = addCreatureReady(player2, archerCard);

        harness.setHand(player1, List.of(new SunClasp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, archer.getId());
        harness.passBothPriorities();

        Permanent clasp = findPermanent(player1, "Sun Clasp");
        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        int claspIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clasp);
        harness.activateAbility(player1, claspIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard() instanceof LongbowArcher);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SunClasp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunClasp);
    }
    @Test
    @CardUsed({SunClasp.class, LongbowArcher.class, AuraFinesse.class, UndiscoveredParadise.class})
    @DisplayName("Moving Sun Clasp in response returns the creature enchanted at resolution")
    void returnsCurrentHostAfterAuraMovesInResponse() {
        Permanent original = addCreatureReady(player1, new LongbowArcher());
        Permanent destination = addCreatureReady(player2, new LongbowArcher());
        destination.getCard().setOwnerId(player2.getId());
        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(original.getId());
        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(new UndiscoveredParadise()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.castInstant(player1, 0, List.of(clasp.getId(), destination.getId()));
        harness.passBothPriorities();
        assertThat(clasp.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(destination);
        assertThat(gd.playerHands.get(player2.getId())).contains(destination.getCard());
        harness.assertInGraveyard(player1, "Sun Clasp");
    }

    @Test
    @DisplayName("Returning a creature controlled by another player still uses its owner")
    void returnsStolenCreatureToOwner() {
        LongbowArcher card = new LongbowArcher();
        card.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player1, card);
        Permanent clasp = harness.addToBattlefieldAndReturn(player1, new SunClasp());
        clasp.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, clasp);
        harness.assertInGraveyard(player1, "Sun Clasp");
    }
}
