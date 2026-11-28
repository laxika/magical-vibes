package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TippyToeTerrificPartner;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfTheWerefox.class, GrizzlyBears.class, LlanowarElves.class, CandyGrapple.class,
        AnointedProcession.class, TippyToeTerrificPartner.class})
class CurseOfTheWerefoxTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Monster Role on the target, then it fights the chosen creature")
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
    @DisplayName("The reflexive fight's target may be declined")
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
    @DisplayName("The Role can't go on an opponent's creature")
    void cannotTargetAnOpponentsCreatureForTheRole() {
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no opponent creature the Role is created and nothing fights")
    void createsTheRoleWithoutAReflexiveFightWhenNoOpponentCreatureExists() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castAndResolveCurse(target);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A second Monster Role replaces the first")
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
    @DisplayName("A target that died in response gets no Role and fights nothing")
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
    @DisplayName("A Role recipient that died in response to the reflexive ability fights nothing")
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
    @DisplayName("The reflexive fight can't target a creature you control")
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
    @DisplayName("A fight target that died in response leaves the Role and deals no damage")
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
    @DisplayName("Both creatures deal fight damage, the Role's bonus included")
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

    @Test
    @DisplayName("After the caster orders the Role creation's replacements, the Role's creature still fights")
    void fightsAfterTheReplacementOrderOfTheRoleCreation() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new LlanowarElves());
        castAndResolveCurse(target);

        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.startsWith("Anointed Procession")).findFirst().orElseThrow());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(findPermanents(player1, "Monster")).isNotEmpty()
                .allSatisfy(role -> assertThat(role.getAttachedTo()).isEqualTo(target.getId()));
    }

    private void castAndResolveCurse(Permanent target) {
        harness.setHand(player1, List.of(new CurseOfTheWerefox()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
