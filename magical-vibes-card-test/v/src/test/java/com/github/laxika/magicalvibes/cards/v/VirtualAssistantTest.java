package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroicTeamwork;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtualAssistant.class, GrizzlyBears.class, HeroicTeamwork.class})
class VirtualAssistantTest extends BaseCardTest {

    @Test
    void createsRobotWhenSpellUsesTeamwork() {
        Permanent assistant = addCreatureReady(player1, new VirtualAssistant());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(teamworkSpell()));

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teammate.getId()));
        resolveAllTriggers();

        Permanent robot = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
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
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void canTapAssistantItselfForTeamworkAndCreatesCorrectlyNamedToken() {
        Permanent assistant = harness.addToBattlefieldAndReturn(player1, new VirtualAssistant());
        harness.setHand(player1, List.of(new HeroicTeamwork()));
        harness.setLibrary(player1, List.of(new VirtualAssistant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifices(player1, 0, assistant.getId(), List.of(assistant.getId()));
        assertThat(assistant.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Robot Hero Token");
    }

    @Test
    void opponentsTeamworkSpellDoesNotTriggerAssistant() {
        addCreatureReady(player1, new VirtualAssistant());
        Permanent teammate = harness.addToBattlefieldAndReturn(player2, new VirtualAssistant());
        harness.setHand(player2, List.of(new HeroicTeamwork()));
        harness.setLibrary(player2, List.of(new VirtualAssistant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifices(player2, 0, teammate.getId(), List.of(teammate.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
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
