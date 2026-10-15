package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergencyWeld.class, Forest.class, GrizzlyBears.class, MindStone.class})
class EmergencyWeldTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an artifact card to hand and creates a Soldier token")
    void returnsArtifactAndCreatesSoldier() {
        Card artifact = new MindStone();

        castWeld(artifact);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(artifact.getId()));
        assertSoldierToken();
    }

    @Test
    @DisplayName("Returns a creature card to hand and creates a Soldier token")
    void returnsCreatureAndCreatesSoldier() {
        Card creature = new GrizzlyBears();

        castWeld(creature);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertSoldierToken();
    }

    @Test
    @DisplayName("Rejects a non-artifact, non-creature graveyard card")
    void rejectsInvalidGraveyardTarget() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new EmergencyWeld()));
        addWeldMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects an artifact in an opponent's graveyard")
    void rejectsOpponentsGraveyardTarget() {
        Card artifact = new MindStone();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new EmergencyWeld()));
        addWeldMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast without a graveyard target just to create a token")
    void requiresGraveyardTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new EmergencyWeld()));
        addWeldMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.<java.util.UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create a token when its only target leaves the graveyard")
    void doesNotCreateTokenWhenTargetBecomesIllegal() {
        Card artifact = new MindStone();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new EmergencyWeld()));
        addWeldMana();
        harness.castSorcery(player1, 0, artifact.getId());
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Emergency Weld");
        assertThat(gd.stack).isEmpty();
    }

    private void castWeld(Card target) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new EmergencyWeld()));
        addWeldMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addWeldMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void assertSoldierToken() {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
    }
}
