package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.c.CopperhornScout;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.StoicRebuttal;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KuldothaRebirth.class, AccordersShield.class, CopperhornScout.class, Memnite.class, StoicRebuttal.class})
class KuldothaRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices an artifact and puts spell on stack")
    void castingSacrificesArtifactAndPutsOnStack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kuldotha Rebirth");

        harness.assertNotOnBattlefield(player1, "Accorder's Shield");
        harness.assertInGraveyard(player1, "Accorder's Shield");
    }

    @Test
    @DisplayName("Resolving creates three 1/1 red Goblin tokens")
    void resolvingCreatesThreeGoblinTokens() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> goblins = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(goblins).hasSize(3);

        for (Permanent goblin : goblins) {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact permanent")
    void cannotSacrificeNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperhornScout());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact")
    void cannotSacrificeOpponentsArtifact() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kuldotha Rebirth");
    }

    @Test
    @DisplayName("Can sacrifice artifact creature as the cost")
    void canSacrificeArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Memnite");
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> goblins = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .toList();
        assertThat(goblins).hasSize(3);
    }

    @Test
    @DisplayName("Countering the spell does not refund the sacrificed artifact or create tokens")
    void counteringDoesNotRefundSacrificeOrCreateTokens() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        KuldothaRebirth rebirth = new KuldothaRebirth();
        harness.setHand(player1, List.of(rebirth));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rebirth.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Accorder's Shield");
        harness.assertInGraveyard(player1, "Kuldotha Rebirth");
    }

    @Test
    @DisplayName("A tapped artifact can be sacrificed and other artifacts are retained")
    void canSacrificeTappedArtifactWithoutSacrificingOthers() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        artifact.tap();
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Accorder's Shield");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(retained).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice cannot be omitted even when an artifact is available")
    void cannotOmitSacrificeWithArtifactAvailable() {
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Accorder's Shield");
        harness.assertInHand(player1, "Kuldotha Rebirth");
    }
}
