package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.ScroungedScythe;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarvestHand.class, ScroungedScythe.class, LightningBolt.class, EliteVanguard.class, GrizzlyBears.class})
class HarvestHandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to the battlefield transformed as Scrounged Scythe when it dies")
    void returnsTransformedOnDeath() {
        harness.addToBattlefield(player1, new HarvestHand());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Harvest Hand"));
        harness.passBothPriorities(); // resolve the death trigger

        Permanent returned = findPermanent(player1, "Scrounged Scythe");
        assertThat(returned.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Harvest Hand");
    }

    @Test
    @DisplayName("Does not return if it has already left the graveyard")
    void doesNotReturnIfNoLongerInGraveyard() {
        harness.addToBattlefield(player1, new HarvestHand());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Harvest Hand"));

        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities(); // resolve the death trigger

        harness.assertNotOnBattlefield(player1, "Scrounged Scythe");
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped Human has menace, equipped non-Human does not")
    void menaceOnlyForHumans() {
        Permanent human = addReadyHuman(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);

        scythe.setAttachedTo(human.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isTrue();

        scythe.setAttachedTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches the Scythe to a creature you control")
    void equipAttachesToCreature() {
        Permanent scythe = addScytheReady(player1);
        Permanent human = addReadyHuman(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isTrue();
    }

    private Permanent addScytheReady(Player player) {
        return addCreatureReady(player, new HarvestHand().getBackFaceCard());
    }

    private Permanent addReadyHuman(Player player) {
        return addCreatureReady(player, new EliteVanguard());
    }

    @Test
    @DisplayName("Returns untapped and unattached even if a Human is available")
    void returnsUntappedAndUnattached() {
        addReadyHuman(player1);
        Permanent hand = harness.addToBattlefieldAndReturn(player1, new HarvestHand());
        hand.tap();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hand.getId());
        harness.passBothPriorities();

        Permanent scythe = findPermanent(player1, "Scrounged Scythe");
        assertThat(scythe.isTapped()).isFalse();
        assertThat(scythe.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Elite Vanguard"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A stolen Harvest Hand returns under its controller's control")
    void returnsUnderControllerRatherThanOwner() {
        Permanent hand = harness.addToBattlefieldAndReturn(player2, new HarvestHand());
        gd.stolenCreatures.put(hand.getId(), player1.getId());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, hand.getId());
        harness.assertInGraveyard(player1, "Harvest Hand");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Scrounged Scythe");
        harness.assertNotOnBattlefield(player1, "Scrounged Scythe");
        harness.assertNotInGraveyard(player1, "Harvest Hand");
    }

    @Test
    @DisplayName("Re-equipping moves the boost and removes menace from the previous creature")
    void reequippingMovesBonuses() {
        Permanent scythe = addScytheReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        scythe.setAttachedTo(human.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        addScytheReady(player1);
        Permanent human = addReadyHuman(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        addScytheReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
