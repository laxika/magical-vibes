package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfTheWerefox.class, GrizzlyBears.class, LlanowarElves.class, CandyGrapple.class})
class CurseOfTheWerefoxTest extends BaseCardTest {

    @Test
    void createsMonsterRoleAndReflexivelyFightsChosenCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineTheFightTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void cannotTargetAnOpponentsCreatureForTheRole() {
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsTheRoleWithoutAReflexiveFightWhenNoOpponentCreatureExists() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveCurse(target);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void replacesThePreviousRoleRatherThanStackingItsBonus() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveCurse(target);
        harness.passBothPriorities();
        Permanent oldRole = findPermanent(player1, "Monster");

        castAndResolveCurse(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monster")).hasSize(1);
        assertThat(findPermanent(player1, "Monster").getId()).isNotEqualTo(oldRole.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void doesNotCreateARoleOrFightIfTheSpellTargetDiesInResponse() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(findPermanents(player1, "Monster")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotFightIfTheRoleRecipientDiesInResponseToTheReflexiveAbility() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);
        harness.handlePermanentChosen(player1, opponent.getId());

        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(opponent.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Monster")).isEmpty();
    }

    @Test
    void cannotChooseACreatureYouControlForTheReflexiveFight() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ally.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(ally.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void keepsTheRoleButDoesNotFightIfTheFightTargetDiesInResponse() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);
        harness.handlePermanentChosen(player1, opponent.getId());

        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void bothCreaturesDealFightDamageUsingTheMonsterBonus() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        castAndResolveCurse(target);
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castAndResolveCurse(Permanent target) {
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
