package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.cards.y.YsgardsCall;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornOfValhalla.class, YsgardsCall.class, SteadfastPaladin.class})
class HornOfValhallaTest extends BaseCardTest {

    @Test
    @DisplayName("Adventure creates X white Soldier tokens")
    void adventureCreatesSoldierTokens() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 3, Map.of());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Soldier");
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each creature the Equipment's controller controls")
    void equippedCreatureScalesWithControlledCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        horn.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Equipment face can be cast from exile after Adventure")
    void equipmentFaceCanBeCastFromExileAfterAdventure() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Horn of Valhalla")).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    @DisplayName("Equip costs three mana and moves the bonus to the new creature")
    void equipMovesBonusToNewCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 2, null, first.getId());
        harness.passBothPriorities();

        assertThat(horn.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(horn.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        harness.addToBattlefield(player1, new HornOfValhalla());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated with only two mana")
    void equipRequiresThreeMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(horn.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting the Equipment directly does not create tokens or attach it")
    void castEquipmentDirectly() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Horn of Valhalla").isAttached()).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    @DisplayName("Bonus changes when creatures enter and leave and ignores opposing creatures")
    void bonusUpdatesWithCreatureCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        horn.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new SteadfastPaladin());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new SteadfastPaladin());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opposing equipped creature counts the Equipment controller's creatures")
    void opposingEquippedCreatureUsesEquipmentControllerCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.addToBattlefield(player2, new SteadfastPaladin());
        harness.addToBattlefield(player1, new SteadfastPaladin());
        Permanent horn = harness.addToBattlefieldAndReturn(player1, new HornOfValhalla());
        horn.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adventure with X zero creates no tokens but still exiles the card")
    void zeroAdventureStillExilesCard() {
        HornOfValhalla card = new HornOfValhalla();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
