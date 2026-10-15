package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EagerGlyphmage.class})
class EagerGlyphmageTest extends BaseCardTest {

    @Test
    @DisplayName("Eager Glyphmage creates a 1/1 flying Inkling token when it enters")
    void etbCreatesFlyingInklingToken() {
        harness.setHand(player1, List.of(new EagerGlyphmage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> inklings = findPermanents(player1, "Inkling");
        assertThat(inklings).hasSize(1);
        Permanent inkling = inklings.getFirst();
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Inkling is created only when the enters ability resolves")
    void tokenCreationWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new EagerGlyphmage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Inkling")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eager Glyphmage");
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(inkling.getCard().isToken()).isTrue();
        assertThat(inkling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(inkling.getCard().getSubtypes()).containsExactly(CardSubtype.INKLING);
        assertThat(inkling.isTapped()).isFalse();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    @Test
    @DisplayName("The other player creates the Inkling when they control Eager Glyphmage")
    void tokenIsCreatedUnderTheEnteringCreaturesControllersControl() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EagerGlyphmage()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Inkling")).hasSize(1);
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }
}
