package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TribalGolem.class, GrizzlyBears.class})
class TribalGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Gains each keyword while its controller has the matching creature type")
    void gainsKeywordsFromControlledCreatureTypes() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addTribe(player1, CardSubtype.BEAST);
        addTribe(player1, CardSubtype.GOBLIN);
        addTribe(player1, CardSubtype.SOLDIER);
        addTribe(player1, CardSubtype.WIZARD);
        addTribe(player1, CardSubtype.ZOMBIE);

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not gain tribal keywords without the matching creature types")
    void doesNotGainKeywordsWithoutMatchingCreatureTypes() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Only controlled creature types grant the tribal keywords")
    void opponentCreatureTypesDoNotGrantKeywords() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addTribe(player2, CardSubtype.BEAST);
        addTribe(player2, CardSubtype.GOBLIN);
        addTribe(player2, CardSubtype.SOLDIER);
        addTribe(player2, CardSubtype.WIZARD);

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Regeneration can be activated while its controller controls a Zombie")
    void regeneratesWithZombie() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addTribe(player1, CardSubtype.ZOMBIE);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration cannot be activated without a Zombie")
    void cannotRegenerateWithoutZombie() {
        addCreatureReady(player1, new TribalGolem());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a Zombie");
    }

    private void addTribe(Player player, CardSubtype subtype) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(subtype));
        addCreatureReady(player, card);
    }
}
