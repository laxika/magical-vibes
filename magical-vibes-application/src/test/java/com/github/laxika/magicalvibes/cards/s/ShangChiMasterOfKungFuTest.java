package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ShangChiMasterOfKungFu.class)
class ShangChiMasterOfKungFuTest extends BaseCardTest {

    @Test
    void activatesSummoningSickCreatureAbilitiesAndAddsAnyChosenColor() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, createAbilitySource(CardType.CREATURE, CardColor.BLUE));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(pool.getCreatureAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotActivateNoncreatureSourceAbilities() {
        harness.addToBattlefield(player1, new ShangChiMasterOfKungFu());
        harness.addToBattlefield(player1, createAbilitySource(CardType.ARTIFACT, CardColor.BLUE));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    private static Card createAbilitySource(CardType type, CardColor color) {
        Card card = new Card();
        card.setName(type == CardType.CREATURE ? "Blue Creature" : "Blue Artifact");
        card.setType(type);
        card.setManaCost("{2}");
        card.setColor(color);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        card.addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new GainLifeEffect(1)),
                "{U}: You gain 1 life."
        ));
        return card;
    }
}
