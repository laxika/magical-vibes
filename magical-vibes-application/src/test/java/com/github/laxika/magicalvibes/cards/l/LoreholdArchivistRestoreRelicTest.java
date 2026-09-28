package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RestoreRelic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoreholdArchivistRestoreRelic.class, RestoreRelic.class, GrizzlyBears.class, MindStone.class, Plains.class})
class LoreholdArchivistRestoreRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared at upkeep with three artifact or creature cards in its graveyard")
    void becomesPreparedAtUpkeepWithArtifactOrCreatureCards() {
        Permanent archivist = addArchivist();
        harness.setGraveyard(player1, List.of(new MindStone(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archivist.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(archivist.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared with fewer than three artifact or creature cards")
    void doesNotBecomePreparedBelowThreshold() {
        Permanent archivist = addArchivist();
        harness.setGraveyard(player1, List.of(new MindStone(), new GrizzlyBears(), new Plains()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(archivist.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("Restore Relic exiles a creature card and creates a token copy")
    void restoreRelicCreatesCreatureTokenCopy() {
        Permanent archivist = prepareArchivist();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castPreparedSpell(archivist, target);

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken() && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(archivist.isPrepared()).isFalse();
        assertThat(archivist.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Restore Relic creates a token copy of an artifact card")
    void restoreRelicCreatesArtifactTokenCopy() {
        Permanent archivist = prepareArchivist();
        Card target = new MindStone();
        harness.setGraveyard(player1, List.of(target));

        castPreparedSpell(archivist, target);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Mind Stone")
                        && permanent.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Restore Relic cannot target a non-artifact, non-creature card")
    void restoreRelicRejectsInvalidGraveyardTarget() {
        Permanent archivist = prepareArchivist();
        Card target = new Plains();
        harness.setGraveyard(player1, List.of(target));
        UUID copyId = archivist.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        addRestoreRelicMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addArchivist() {
        return addCreatureReady(player1, new LoreholdArchivistRestoreRelic());
    }

    private Permanent prepareArchivist() {
        Permanent archivist = addArchivist();
        harness.setGraveyard(player1, List.of(new MindStone(), new GrizzlyBears(), new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(archivist.isPrepared()).isTrue();
        return archivist;
    }

    private void castPreparedSpell(Permanent archivist, Card target) {
        harness.forceActivePlayer(player1);
        addRestoreRelicMana();
        harness.castFromExile(player1, archivist.getPreparedSpellCardId(), target.getId());
        harness.passBothPriorities();
    }

    private void addRestoreRelicMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
