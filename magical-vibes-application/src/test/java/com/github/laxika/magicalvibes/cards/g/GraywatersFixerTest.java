package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GraywatersFixer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraywatersFixer.class, GrizzlyBears.class})
@DisplayName("Graywater's Fixer")
class GraywatersFixerTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Grants encore to outlaw creature cards in your graveyard")
    void grantsEncoreToOutlawCreatureCards(CardSubtype outlawSubtype) {
        Card outlaw = new GrizzlyBears();
        outlaw.setSubtypes(List.of(outlawSubtype));
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(outlaw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant encore to non-outlaw creature cards")
    void doesNotGrantEncoreToNonOutlawCreatureCards() {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
