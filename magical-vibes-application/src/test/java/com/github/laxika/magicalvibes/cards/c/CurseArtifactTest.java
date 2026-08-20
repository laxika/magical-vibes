package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseArtifact.class})
class CurseArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = addArtifact(player2);

        harness.setHand(player1, List.of(new CurseArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse Artifact")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-artifact")
    void cannotEnchantNonArtifact() {
        addArtifact(player2);
        Card creatureCard = new Card();
        creatureCard.setName("Test Creature");
        creatureCard.setType(CardType.CREATURE);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new CurseArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Accepting the upkeep choice sacrifices the enchanted artifact")
    void acceptingChoiceSacrificesEnchantedArtifact() {
        Permanent artifact = addArtifact(player2);
        Permanent otherArtifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Test Artifact"));
    }

    @Test
    @DisplayName("Declining the upkeep choice deals 2 damage to the artifact's controller")
    void decliningChoiceDealsDamage() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void attachCurseArtifact(Permanent artifact) {
        Permanent curse = new Permanent(new CurseArtifact());
        curse.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
    }

    private Permanent addArtifact(Player player) {
        Card card = new Card();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent artifact = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(artifact);
        return artifact;
    }
}
