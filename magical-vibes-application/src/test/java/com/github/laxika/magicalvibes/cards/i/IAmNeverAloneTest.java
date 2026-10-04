package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlharuSolemnRitualist;
import com.github.laxika.magicalvibes.cards.o.OmarthisGhostfireInitiate;
import com.github.laxika.magicalvibes.cards.t.ThePrismaticPiper;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IAmNeverAlone.class, ThePrismaticPiper.class,
        AlharuSolemnRitualist.class, OmarthisGhostfireInitiate.class})
class IAmNeverAloneTest extends BaseCardTest {

    @Test
    void createsNonlegendaryTokenCopyOfCommanderInCommandZone() {
        ThePrismaticPiper commander = prepareCommanderInCommandZone();

        resolveScheme();

        Permanent token = findPermanents(player1, commander.getName()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.isCommander(token.getCard().getId())).isFalse();
    }

    @Test
    void copiesCommanderPermanentAndPreservesItsPrintedCharacteristics() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);

        resolveScheme();

        Permanent token = findPermanents(player1, commanderPermanent.getCard().getName()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(commanderPermanent.getEffectivePower());
        assertThat(token.getEffectiveToughness()).isEqualTo(commanderPermanent.getEffectiveToughness());
    }

    @Test
    void doesNotCopyCountersOrTappedState() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        commanderPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        commanderPermanent.tap();

        resolveScheme();

        Permanent token = findToken(commander.getName());
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getEffectivePower()).isEqualTo(commanderPermanent.getEffectivePower() - 2);
        assertThat(token.getEffectiveToughness()).isEqualTo(commanderPermanent.getEffectiveToughness() - 2);
    }

    @Test
    void copiesCommanderControlledByOpponentUnderSchemeControllersControl() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);

        resolveScheme();

        assertThat(findToken(commander.getName())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void findsCommanderInLibrary() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        harness.setLibrary(player1, List.of(commander));

        resolveScheme();

        assertThat(findToken(commander.getName())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void findsCommanderInHand() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));

        resolveScheme();

        assertThat(findToken(commander.getName())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void findsCommanderInGraveyard() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        harness.setGraveyard(player1, List.of(commander));

        resolveScheme();

        assertThat(findToken(commander.getName())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(commander);
    }

    @Test
    void copiesCommandersCurrentCopyEffectInsteadOfItsOriginalCard() {
        AlharuSolemnRitualist commander = new AlharuSolemnRitualist();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        ThePrismaticPiper copiedCard = new ThePrismaticPiper();
        commanderPermanent.setCard(copiedCard);

        resolveScheme();

        Permanent token = findToken(copiedCard.getName());
        assertThat(token.getEffectivePower()).isEqualTo(commanderPermanent.getEffectivePower());
        assertThat(token.getEffectiveToughness()).isEqualTo(commanderPermanent.getEffectiveToughness());
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.isCommander(token.getCard().getId())).isFalse();
    }

    @Test
    void copiedCommandersEntersAbilityTriggers() {
        AlharuSolemnRitualist commander = new AlharuSolemnRitualist();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        Permanent otherCreature = addCreatureReady(player1, new ThePrismaticPiper());

        resolveScheme();
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findToken(commander.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void allowsControllerToChooseBetweenPartnerCommanders() {
        ThePrismaticPiper first = new ThePrismaticPiper();
        AlharuSolemnRitualist second = new AlharuSolemnRitualist();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(first, second)));
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(first, second)));

        resolveScheme();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copiesFaceDownCommandersFaceDownCharacteristics() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        commanderPermanent.setFaceDownAsCloaked();

        resolveScheme();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.isFaceDown()).isFalse();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void preservesChosenXWhenCopyingCommanderSpellOnStack() {
        OmarthisGhostfireInitiate commander = new OmarthisGhostfireInitiate();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, 3);

        resolveScheme();

        Permanent token = findToken(commander.getName());
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void doesNothingWithoutACommander() {
        resolveScheme();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private ThePrismaticPiper prepareCommanderInCommandZone() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private Permanent findToken(String name) {
        return findPermanents(player1, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void resolveScheme() {
        IAmNeverAlone scheme = new IAmNeverAlone();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
