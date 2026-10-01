package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castIndrikStomphowler(Permanent target) {
        castIndrikSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void castIndrikSpell() {
        harness.setHand(player1, List.of(new IndrikStomphowler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
