package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndriderSpikespitter.class, Forest.class})
class EndriderSpikespitterTest extends BaseCardTest {

    @Test
    void atMaxSpeedExilesTopCardWithPlayPermission() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    void belowMaxSpeedDoesNotExileTopCard() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void triggeredAbilityStillExilesWhenSpeedDropsBeforeResolution() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
    }

    @Test
    void exiledLandCanBePlayedDuringMainPhase() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new EndriderSpikespitter();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Endrider Spikespitter")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void unplayedCardRemainsExiledButPermissionExpiresAfterTurn() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        Card top = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    void emptyLibraryDoesNotCauseLossWhenUpkeepAbilityResolves() {
        harness.addToBattlefield(player1, new EndriderSpikespitter());
        harness.setLibrary(player1, List.of());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }
}
