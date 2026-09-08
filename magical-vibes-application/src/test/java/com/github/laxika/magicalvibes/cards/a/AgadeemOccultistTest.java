package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgadeemOccultistTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature whose mana value is within the Ally count")
    void reanimatesCreatureWithinAllyCount() {
        addOccultist();
        Card target = creatureCard("Low-Mana Creature", "{1}");
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Counts all Allies you control when checking the mana value limit")
    void countsAlliesForManaValueLimit() {
        addOccultist();
        addAlly();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Rejects a creature whose mana value exceeds the Ally count")
    void rejectsCreatureAboveAllyCount() {
        addOccultist();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value must be 1 or less");
    }

    @Test
    @DisplayName("Rechecks the Ally count when the ability resolves")
    void rechecksAllyCountOnResolution() {
        addOccultist();
        Permanent extraAlly = addAlly();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(extraAlly);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    private Permanent addOccultist() {
        Permanent occultist = new Permanent(new AgadeemOccultist());
        occultist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(occultist);
        return occultist;
    }

    private Permanent addAlly() {
        Card ally = creatureCard("Test Ally", "{1}");
        ally.setSubtypes(List.of(CardSubtype.ALLY));
        Permanent permanent = new Permanent(ally);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Card creatureCard(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
