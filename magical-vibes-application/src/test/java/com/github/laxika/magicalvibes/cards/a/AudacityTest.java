package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Audacity.class, ArgothianSprite.class, EnergyRefractor.class})
class AudacityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0 and trample")
    void grantsBoostAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new Audacity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Audacity());
        aura.setAttachedTo(bears.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Audacity");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setHand(player1, List.of(new Audacity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without boosting other creatures")
    void enchantsOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new Audacity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Draws when the enchanted creature leaves and the unattached Aura goes to the graveyard")
    void drawsWhenEnchantedCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Audacity());
        aura.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Audacity");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The Aura's last controller draws even when its owner is another player")
    void lastControllerDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Audacity());
        aura.setAttachedTo(creature.getId());
        gd.stolenCreatures.put(aura.getId(), player1.getId());
        int ownerHandBefore = gd.playerHands.get(player1.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Audacity");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
    }

    @Test
    @DisplayName("Exiling the Aura does not draw and removes its boost and trample")
    void exileDoesNotDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Audacity());
        aura.setAttachedTo(creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An Aura whose target disappears before resolution does not draw")
    void invalidTargetDoesNotDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new Audacity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Audacity");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
