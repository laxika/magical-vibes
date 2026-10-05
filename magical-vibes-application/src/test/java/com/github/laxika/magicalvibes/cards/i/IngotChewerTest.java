package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IngotChewer.class, SpringleafDrum.class, GoldmeadowHarrier.class})
class IngotChewerTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: ETB destroys target artifact and Ingot Chewer stays")
    void hardcastDestroysArtifactAndStays() {
        harness.addToBattlefield(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Springleaf Drum");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertInGraveyard(player2, "Springleaf Drum");
        harness.assertOnBattlefield(player1, "Ingot Chewer");
    }

    @Test
    @DisplayName("Evoke: paying {R}, ETB destroys the artifact and Ingot Chewer is sacrificed")
    void evokeDestroysAndSacrificesSelf() {
        harness.addToBattlefield(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Springleaf Drum");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Springleaf Drum");
        harness.assertNotOnBattlefield(player1, "Ingot Chewer");
        harness.assertInGraveyard(player1, "Ingot Chewer");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new GoldmeadowHarrier());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID creatureId = harness.getPermanentId(player2, "Goldmeadow Harrier");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hardcasting without any artifacts is legal and the creature remains")
    void hardcastWithoutArtifacts() {
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ingot Chewer");
        harness.assertNotInGraveyard(player1, "Ingot Chewer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evoking without any artifacts still sacrifices Ingot Chewer")
    void evokeWithoutArtifactsStillSacrifices() {
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ingot Chewer");
        harness.assertInGraveyard(player1, "Ingot Chewer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mandatory destruction ability can target your own artifact")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Springleaf Drum"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Springleaf Drum");
        harness.assertOnBattlefield(player1, "Ingot Chewer");
    }

    @Test
    @DisplayName("The controller can resolve artifact destruction before evoke sacrifice")
    void controllerChoosesDestructionBeforeSacrifice() {
        harness.addToBattlefield(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithEvoke(player1, 0, harness.getPermanentId(player2, "Springleaf Drum"));
        harness.passBothPriorities();

        var order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        assertThat(gd.stack).hasSize(2);
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("sacrifice")).findFirst().orElseThrow());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Springleaf Drum");
        harness.assertOnBattlefield(player1, "Ingot Chewer");
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Ingot Chewer");
    }

    @Test
    @DisplayName("Losing the artifact target does not counter the independent evoke sacrifice")
    void missingArtifactTargetDoesNotPreventSacrifice() {
        harness.addToBattlefield(player2, new SpringleafDrum());
        harness.setHand(player1, List.of(new IngotChewer()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player2, "Springleaf Drum");

        harness.castCreatureWithEvoke(player1, 0, targetId);
        harness.passBothPriorities();
        var artifact = findPermanent(player2, "Springleaf Drum");
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerHands.get(player2.getId()).add(artifact.getCard());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ingot Chewer");
        harness.assertNotInGraveyard(player2, "Springleaf Drum");
        assertThat(gd.playerHands.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Override
    protected void resolveAllTriggers() {
        var order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (order != null && order.context()
                instanceof com.github.laxika.magicalvibes.model.ChoiceContext.SpellCastTriggerOrder) {
            harness.handleListChoice(player1, order.options().getFirst());
        }
        super.resolveAllTriggers();
    }
}
