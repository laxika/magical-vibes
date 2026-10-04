package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EsikasChariot.class, SerraAngel.class, GrizzlyBears.class, GoldveinPick.class})
class EsikasChariotTest extends BaseCardTest {

    @Test
    void entersWithTwoCatTokens() {
        harness.setHand(player1, List.of(new EsikasChariot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allMatch(cat -> cat.getCard().isToken());
        assertThat(cats).allMatch(cat -> cat.getEffectivePower() == 2 && cat.getEffectiveToughness() == 2);
    }

    @Test
    void attackCreatesCopyOfTargetTokenYouControl() {
        Permanent chariot = addChariotReady();
        Permanent crew = addCreatureReady(player1, new SerraAngel());
        Permanent targetToken = addTokenCreature("Cat", 2, 2, CardColor.GREEN, CardSubtype.CAT);
        targetToken.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, targetToken.getId());
        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).filteredOn(cat -> cat.getCard().isToken()).hasSize(2);
        assertThat(chariot.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackCannotTargetNonTokenPermanent() {
        addChariotReady();
        addCreatureReady(player1, new SerraAngel());
        Permanent targetToken = addTokenCreature("Cat", 2, 2, CardColor.GREEN, CardSubtype.CAT);
        targetToken.tap();
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());
        nonToken.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChariotReady() {
        return addCreatureReady(player1, new EsikasChariot());
    }

    @Test
    void freshlyCreatedCatsCanCrewButFreshChariotCannotAttack() {
        Permanent chariot = castChariotWithCats();
        List<Permanent> cats = findPermanents(player1, "Cat");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cats).allMatch(Permanent::isTapped);
        assertThat(chariot.isAnimatedUntilEndOfTurn()).isTrue();
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewRequiresAtLeastFourPower() {
        addChariotReady();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyDoesNotCopyCountersTappedStateOrAttackingState() {
        Permanent chariot = castChariotWithCats();
        chariot.setSummoningSick(false);
        Permanent target = findPermanents(player1, "Cat").getFirst();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(3);
        Permanent copy = cats.getLast();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isAttacking()).isFalse();
    }

    @Test
    void attackCannotTargetOpponentsToken() {
        Permanent chariot = castChariotWithCats();
        chariot.setSummoningSick(false);
        Permanent opposingToken = findPermanents(player1, "Cat").getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(opposingToken);
        gd.playerBattlefields.get(player2.getId()).add(opposingToken);
        addCreatureReady(player1, new SerraAngel());
        findPermanents(player1, "Cat").forEach(Permanent::tap);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingToken.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackCreatesNoCopyWhenTargetLeavesBattlefield() {
        Permanent chariot = castChariotWithCats();
        chariot.setSummoningSick(false);
        Permanent target = findPermanents(player1, "Cat").getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
    }

    private Permanent castChariotWithCats() {
        harness.setHand(player1, List.of(new EsikasChariot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Esika's Chariot");
    }

    @Test
    void attackTriggerStillCreatesCopyAfterChariotLeavesBattlefield() {
        Permanent chariot = castChariotWithCats();
        chariot.setSummoningSick(false);
        Permanent target = findPermanents(player1, "Cat").getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(chariot);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cat")).hasSize(3);
    }

    @Test
    void attackCreatesNoCopyWhenTargetChangesController() {
        Permanent chariot = castChariotWithCats();
        chariot.setSummoningSick(false);
        Permanent target = findPermanents(player1, "Cat").getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cat")).hasSize(1);
        assertThat(findPermanents(player2, "Cat")).hasSize(1);
    }

    @Test
    void attackWithNoTokensDoesNotCreateACopy() {
        addChariotReady();
        addCreatureReady(player1, new SerraAngel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackCanCopyANoncreatureTreasureToken() {
        addChariotReady();
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent pick = addCreatureReady(player1, new GoldveinPick());
        pick.setAttachedTo(angel.getId());
        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        Permanent copy = findPermanents(player1, "Treasure").getLast();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addTokenCreature(String name, int power, int toughness, CardColor color,
                                       CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        return addCreatureReady(player1, card);
    }
}
