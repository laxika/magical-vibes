package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.e.EpharaGodOfThePolis;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnravelTheAether.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class, EpharaGodOfThePolis.class})
class UnravelTheAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves by shuffling a target artifact into its owner's library")
    void shufflesArtifactIntoLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Resolves by shuffling a target enchantment into its owner's library")
    void shufflesEnchantmentIntoLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelicChorus()).getId();
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A permanent controlled by another player goes to its owner's library")
    void shufflesIntoOwnerLibraryInsteadOfControllerLibrary() {
        FountainOfYouth artifact = new FountainOfYouth();
        artifact.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, artifact).getId();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Can target an artifact controlled by the caster")
    void shufflesOwnArtifactIntoLibrary() {
        FountainOfYouth artifact = new FountainOfYouth();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, artifact).getId();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("Does not shuffle a target that left the battlefield before resolution")
    void targetLeavingBattlefieldMakesSpellFailToResolve() {
        var target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Unravel the Aether");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shuffles an indestructible God into its owner's library")
    void shufflesIndestructibleGodIntoLibrary() {
        EpharaGodOfThePolis god = new EpharaGodOfThePolis();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, god).getId();
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnravelTheAether()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ephara, God of the Polis");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(god);
        harness.assertNotInGraveyard(player2, "Ephara, God of the Polis");
    }
}
