package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndrikStomphowler.class, AzoriusSignet.class, SealOfDoom.class, MistralCharger.class})
class IndrikStomphowlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        castIndrikStomphowler(artifact);

        harness.assertInGraveyard(player2, "Azorius Signet");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
    }

    @Test
    @DisplayName("Enters by destroying a target enchantment")
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());

        castIndrikStomphowler(enchantment);

        harness.assertInGraveyard(player2, "Seal of Doom");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
    }

    @Test
    @DisplayName("The ETB ability may destroy an artifact you control")
    void destroysOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());

        castIndrikStomphowler(artifact);

        harness.assertInGraveyard(player1, "Azorius Signet");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
    }

    @Test
    @DisplayName("The ETB target choice excludes creatures")
    void excludesCreaturesFromTargetChoice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MistralCharger());

        castIndrikSpell();

        PendingInteraction.PermanentChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(artifact.getId()).doesNotContain(creature.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Azorius Signet");
        harness.assertOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("The ETB ability is skipped when no legal target exists")
    void skipsEtbWhenNoLegalTargetExists() {
        harness.addToBattlefield(player2, new MistralCharger());

        castIndrikSpell();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
        harness.assertOnBattlefield(player2, "Mistral Charger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The destruction trigger resolves even after Stomphowler dies")
    void triggerResolvesAfterSourceDies() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());
        harness.addToBattlefield(player2, new SealOfDoom());

        castIndrikSpell();
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.activateAbility(player2, 1, null,
                harness.getPermanentId(player1, "Indrik Stomphowler"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Indrik Stomphowler");
        harness.assertOnBattlefield(player2, "Azorius Signet");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Azorius Signet");
        harness.assertInGraveyard(player2, "Seal of Doom");
    }

    @Test
    @DisplayName("The trigger does not choose a replacement when its target is sacrificed")
    void sacrificedTargetDoesNotCauseRetargeting() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfDoom());
        harness.addToBattlefield(player2, new AzoriusSignet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MistralCharger());

        castIndrikSpell();
        harness.handlePermanentChosen(player1, enchantment.getId());

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Seal of Doom");
        harness.assertInGraveyard(player1, "Mistral Charger");
        harness.assertOnBattlefield(player2, "Azorius Signet");
        harness.assertOnBattlefield(player1, "Indrik Stomphowler");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void castIndrikStomphowler(Permanent target) {
        castIndrikSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void castIndrikSpell() {
        harness.castFromHand(player1, new IndrikStomphowler(), "{4}{G}");
        harness.passBothPriorities();
    }
}
