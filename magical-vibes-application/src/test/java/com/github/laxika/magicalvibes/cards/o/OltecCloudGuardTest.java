package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OltecCloudGuard.class})
class OltecCloudGuardTest extends BaseCardTest {

    @Test
    void createsColorlessGnomeArtifactCreatureTokenWhenItEnters() {
        harness.castFromHand(player1, new OltecCloudGuard(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Gnome");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GNOME);
    }

    @Test
    void createsTokenOnlyWhenEnterTriggerResolves() {
        harness.castFromHand(player1, new OltecCloudGuard(), "{3}{W}");

        assertThat(countPermanents(player1, "Gnome")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Oltec Cloud Guard")).isEqualTo(1);
        assertThat(countPermanents(player1, "Gnome")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gnome")).isEqualTo(1);
        assertThat(countPermanents(player2, "Gnome")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastCreatesTokenForItsController() {
        harness.enterBattlefieldAndReturn(player2, new OltecCloudGuard());

        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Gnome")).isEqualTo(1);
        assertThat(countPermanents(player1, "Gnome")).isZero();
        assertThat(findPermanent(player2, "Gnome").isTapped()).isFalse();
    }
}
