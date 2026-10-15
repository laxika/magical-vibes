package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiredSkypainterMaestrosGift.class, GrizzlyBears.class})
class InspiredSkypainterMaestrosGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Inspired Skypainter enters prepared")
    void entersPrepared() {
        Permanent skypainter = castSkypainter();

        assertThat(skypainter.isPrepared()).isTrue();
        UUID copyId = skypainter.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId).card().getName()).isEqualTo("Maestro's Gift");
    }

    @Test
    @DisplayName("Maestro's Gift creates a hasty token copy and unprepares Inspired Skypainter")
    void prepareSpellCreatesHastyTokenCopyAndUnpreparesSource() {
        Permanent skypainter = castSkypainter();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        UUID copyId = skypainter.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, copyId, bears.getId());
        harness.passBothPriorities();

        assertThat(skypainter.isPrepared()).isFalse();
        assertThat(skypainter.getPreparedSpellCardId()).isNull();
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from a creature token prepares Inspired Skypainter")
    void tokenCombatDamagePreparesSource() {
        Permanent skypainter = addCreatureReady(player1, new InspiredSkypainterMaestrosGift());
        Card tokenCard = new Card();
        tokenCard.setName("Token Bear");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(skypainter.isPrepared()).isTrue();
        assertThat(skypainter.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Maestro's Gift cannot target an opponent's creature")
    void prepareSpellRejectsOpponentCreatureTarget() {
        Permanent skypainter = castSkypainter();
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = skypainter.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castSkypainter() {
        harness.setHand(player1, List.of(new InspiredSkypainterMaestrosGift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        return findPermanent(player1, "Inspired Skypainter");
    }

    @Test
    @DisplayName("Inspired Skypainter prepares only when its enters trigger resolves")
    void preparationWaitsForEntersTrigger() {
        harness.setHand(player1, List.of(new InspiredSkypainterMaestrosGift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent skypainter = findPermanent(player1, "Inspired Skypainter");
        assertThat(skypainter.isPrepared()).isFalse();
        assertThat(skypainter.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(skypainter.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("Maestro's Gift haste expires while its token remains on the battlefield")
    void tokenHasteExpiresAfterTurn() {
        Permanent skypainter = castSkypainter();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, skypainter.getPreparedSpellCardId(), skypainter.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Inspired Skypainter").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(token.isPrepared()).isTrue();
        assertThat(skypainter.isPrepared()).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A nontoken creature's combat damage does not prepare Inspired Skypainter")
    void nontokenCombatDamageDoesNotPrepareSource() {
        Permanent skypainter = addCreatureReady(player1, new InspiredSkypainterMaestrosGift());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(skypainter)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(skypainter.isPrepared()).isFalse();
        assertThat(skypainter.getPreparedSpellCardId()).isNull();
    }
}
