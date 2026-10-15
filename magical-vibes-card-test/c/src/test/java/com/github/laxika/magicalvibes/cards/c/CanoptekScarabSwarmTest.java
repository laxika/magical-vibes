package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SanguinaryPriest;
import com.github.laxika.magicalvibes.cards.t.TormodTheDesecrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanoptekScarabSwarm.class, Forest.class, SanguinaryPriest.class, TormodTheDesecrator.class})
class CanoptekScarabSwarmTest extends BaseCardTest {

    @Test
    void exilesTargetPlayersGraveyardAndCreatesAnInsectForEachArtifactOrLand() {
        Card artifact = new CanoptekScarabSwarm();
        Card land = new Forest();
        Card creature = new SanguinaryPriest();
        harness.setGraveyard(player2, List.of(artifact, land, creature));

        castCanoptekScarabSwarm(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(artifact, land, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2);
    }

    @Test
    void createsNoTokensWhenTargetPlayersGraveyardHasNoArtifactsOrLands() {
        Card creature = new SanguinaryPriest();
        harness.setGraveyard(player2, List.of(creature));

        castCanoptekScarabSwarm(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
    }

    @Test
    void canTargetOwnGraveyardAndCreatesColorlessFlyingArtifactInsects() {
        Card artifact = new CanoptekScarabSwarm();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(artifact, land));

        castCanoptekScarabSwarm(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(artifact, land);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    Card token = permanent.getCard();
                    assertThat(token.hasType(CardType.ARTIFACT)).isTrue();
                    assertThat(token.hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getSubtypes()).containsExactly(CardSubtype.INSECT);
                    assertThat(token.getColors()).isEmpty();
                    assertThat(token.getPower()).isEqualTo(1);
                    assertThat(token.getToughness()).isEqualTo(1);
                    assertThat(token.getKeywords()).contains(Keyword.FLYING);
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyGraveyardCreatesNoTokens() {
        harness.setGraveyard(player2, List.of());

        castCanoptekScarabSwarm(player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    void exilesMixedGraveyardAsOneEvent() {
        harness.addToBattlefield(player1, new TormodTheDesecrator());
        harness.setGraveyard(player1, List.of(new CanoptekScarabSwarm(), new SanguinaryPriest()));

        castCanoptekScarabSwarm(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }
    private void castCanoptekScarabSwarm(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CanoptekScarabSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
