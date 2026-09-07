package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreErrant.class, YouthfulKnight.class, GrizzlyBears.class})
class OgreErrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger only targets another attacking Knight")
    void attackTriggerRestrictsTargets() {
        Permanent ogre = addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(knight.getId())
                .doesNotContain(ogre.getId(), nonKnight.getId());
    }

    @Test
    @DisplayName("Attack trigger grants menace until end of turn")
    void attackTriggerGrantsMenace() {
        addCreatureReady(player1, new OgreErrant());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getGrantedKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Attack trigger rejects an attacking non-Knight")
    void rejectsAttackingNonKnight() {
        addCreatureReady(player1, new OgreErrant());
        Permanent nonKnight = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonKnight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
