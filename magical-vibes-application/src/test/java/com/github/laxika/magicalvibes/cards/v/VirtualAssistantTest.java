package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtualAssistant.class, GrizzlyBears.class})
class VirtualAssistantTest extends BaseCardTest {

    @Test
    void createsRobotWhenSpellUsesTeamwork() {
        Permanent assistant = addCreatureReady(player1, new VirtualAssistant());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(teamworkSpell()));

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teammate.getId()));
        resolveAllTriggers();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().isToken()).isTrue();
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ROBOT, CardSubtype.HERO);
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, robot, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
        assertThat(assistant.isTapped()).isFalse();
    }

    @Test
    void doesNotCreateRobotWhenTeamworkIsDeclined() {
        addCreatureReady(player1, new VirtualAssistant());
        harness.setHand(player1, List.of(teamworkSpell()));

        harness.castInstantWithSacrifices(player1, 0, null, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Robot"));
    }

    private Card teamworkSpell() {
        Card spell = new Card();
        spell.setName("Teamwork test spell");
        spell.setType(CardType.INSTANT);
        spell.setManaCost("{0}");
        spell.addEffect(EffectSlot.SPELL, new TeamworkCost(2));
        return spell;
    }
}
