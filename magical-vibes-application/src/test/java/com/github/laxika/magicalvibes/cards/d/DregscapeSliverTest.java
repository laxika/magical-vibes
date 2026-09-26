package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuscleSliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Dregscape Sliver")
@CardUsed({DregscapeSliver.class, MuscleSliver.class, GrizzlyBears.class, BonescytheSliver.class})
class DregscapeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants unearth to Sliver creature cards in its controller's graveyard")
    void grantsUnearthToSliversInGraveyard() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new BonescytheSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Bonescythe Sliver");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Does not grant unearth to non-Sliver creature cards")
    void doesNotGrantUnearthToNonSlivers() {
        harness.addToBattlefield(player1, new DregscapeSliver());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
