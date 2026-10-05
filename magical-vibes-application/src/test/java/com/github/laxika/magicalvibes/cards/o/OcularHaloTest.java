package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OcularHalo.class, MistralCharger.class, AzoriusSignet.class})
class OcularHaloTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to draw a card")
    void enchantedCreatureCanTapToDraw() {
        Permanent creature = addCreatureWithAura();
        harness.setLibrary(player1, List.of(new MistralCharger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Mistral Charger");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted opponent's creature can tap to draw a card")
    void enchantedOpponentCreatureCanTapToDraw() {
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OcularHalo());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player2, List.of(new MistralCharger()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Mistral Charger");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White activation gives the enchanted creature vigilance until end of turn")
    void whiteActivationGrantsVigilanceUntilEndOfTurn() {
        Permanent creature = addCreatureWithAura();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature loses the granted draw ability when Ocular Halo becomes unattached")
    void losesGrantedAbilityWhenUnattached() {
        addCreatureWithAura();
        Permanent aura = findPermanent(player1, "Ocular Halo");
        aura.setAttachedTo(null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Ocular Halo can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new OcularHalo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ocular Halo resolves attached to an opponent's creature")
    void canCastOnOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        harness.setHand(player1, List.of(new OcularHalo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ocular Halo").getAttachedTo()).isEqualTo(creature.getId());
        harness.setLibrary(player2, List.of(new MistralCharger()));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Mistral Charger");
        harness.assertNotInHand(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the granted tap ability")
    void summoningSicknessPreventsDraw() {
        Permanent creature = addCreatureWithAura();
        creature.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped enchanted creature cannot activate the draw ability")
    void tappedCreatureCannotDraw() {
        Permanent creature = addCreatureWithAura();
        creature.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An activated draw ability still resolves after Ocular Halo leaves")
    void drawResolvesAfterAuraLeaves() {
        addCreatureWithAura();
        harness.setLibrary(player1, List.of(new MistralCharger()));
        harness.activateAbility(player1, 0, null, null);

        findPermanent(player1, "Ocular Halo").setAttachedTo(null);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Ocular Halo");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mistral Charger");
    }

    @Test
    @DisplayName("Aura controller can grant an opponent's creature vigilance that persists after the Aura leaves")
    void vigilanceOnOpponentCreaturePersistsAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OcularHalo());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        aura.setAttachedTo(null);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Ocular Halo");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addCreatureWithAura() {
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OcularHalo());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
