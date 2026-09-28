package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        harness.passBothPriorities();

        return findPermanent(player1, "Inspired Skypainter");
    }
}
