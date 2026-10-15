package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptivatingCave;
import com.github.laxika.magicalvibes.cards.c.CutthroatCenturion;
import com.github.laxika.magicalvibes.cards.i.IronhoofBoar;
import com.github.laxika.magicalvibes.cards.o.OakenSiren;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LocusOfEnlightenment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEnigmaJewel.class, LocusOfEnlightenment.class, CutthroatCenturion.class,
        Forest.class, GrizzlyBears.class, CaptivatingCave.class, IronhoofBoar.class, OakenSiren.class, QuintoriusKand.class})
class TheEnigmaJewelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        TheEnigmaJewel jewel = new TheEnigmaJewel();
        harness.castFromHand(player1, jewel, "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(Permanent::isTapped).isEqualTo(true);
    }

    @Test
    @DisplayName("Craft accepts four nonlands with activated abilities from the battlefield and graveyard")
    void craftsWithMixedActivatedAbilityMaterials() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new TheEnigmaJewel());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        Card graveyardCenturion = new CutthroatCenturion();
        harness.setGraveyard(player1, List.of(graveyardCenturion, new Forest(), new GrizzlyBears()));

        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jewel);
        assertThat(locus.getCard()).isInstanceOf(LocusOfEnlightenment.class);
        assertThat(gd.getCardsExiledByPermanent(locus.getId()))
                .hasSize(4)
                .allMatch(card -> card instanceof CutthroatCenturion);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Gained activated abilities are copied and each gained ability is usable only once each turn")
    void copiesGainedAbilityAndLimitsEachAbility() {
        Permanent jewel = harness.addToBattlefieldAndReturn(player1, new TheEnigmaJewel());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        harness.addToBattlefield(player1, new CutthroatCenturion());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jewel);
        assertThat(locus.getCard()).isInstanceOf(LocusOfEnlightenment.class);
        harness.clearPriorityPassed();
        int locusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locus);
        harness.activateAbility(player1, locusIndex, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        resolveAllTriggers();

        assertThat(locus.getPowerModifier()).isEqualTo(4);
        assertThat(locus.getToughnessModifier()).isEqualTo(4);

        harness.clearPriorityPassed();
        int updatedLocusIndex = gd.playerBattlefields.get(player1.getId()).indexOf(locus);
        assertThatThrownBy(() -> harness.activateAbility(player1, updatedLocusIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void restrictedManaCannotPayForSpells() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new OakenSiren()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS))
                .isEqualTo(2);
    }

    @Test
    void restrictedManaCanPayForCraft() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new OakenSiren(), new OakenSiren(),
                new OakenSiren(), new OakenSiren()));
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Locus of Enlightenment").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS))
                .isZero();
    }

    @Test
    void cannotCraftWithOnlyThreeOtherMaterials() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new OakenSiren(), new OakenSiren(), new OakenSiren()));
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "The Enigma Jewel")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void cannotCraftOutsideMainPhase() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new OakenSiren(), new OakenSiren(),
                new OakenSiren(), new OakenSiren()));
        addCraftMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "The Enigma Jewel")).isEqualTo(1);
    }

    @Test
    void canCraftWithMoreThanFourMaterials() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        List<Card> materials = List.of(new OakenSiren(), new OakenSiren(),
                new OakenSiren(), new OakenSiren(), new OakenSiren());
        harness.setGraveyard(player1, materials);
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, materials.stream().map(Card::getId).toList());
        resolveAllTriggers();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.getCardsExiledByPermanent(locus.getId())).containsExactlyInAnyOrderElementsOf(materials);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void channelCardsInGraveyardAreLegalCraftMaterials() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new IronhoofBoar(), new IronhoofBoar(),
                new IronhoofBoar(), new IronhoofBoar()));
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.getCardsExiledByPermanent(locus.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void channelCardsOnBattlefieldAreLegalCraftMaterials() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new IronhoofBoar());
        }
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        assertThat(gd.getCardsExiledByPermanent(locus.getId())).hasSize(4);
        assertThat(countPermanents(player1, "Ironhoof Boar")).isZero();
    }

    @Test
    void gainedManaAbilitiesAreNotCopiedAndDuplicateInstancesHaveSeparateLimits() {
        Permanent locus = craftWithSirens();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
        locus.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    void copiesOtherPermanentsAbilitiesAndAllowsNewTargets() {
        craftWithSirens();
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new CaptivatingCave());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new OakenSiren());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player1, new OakenSiren());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 2, null, originalTarget.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copyTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cave.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsAbilities() {
        craftWithSirens();
        harness.addToBattlefield(player2, new CaptivatingCave());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OakenSiren());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, 2, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void gainedAbilityLimitResetsOnNextTurn() {
        Permanent locus = craftWithSirens();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        locus.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(locus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsLoyaltyAbilitiesButCanActivateOnlyOneLoyaltyAbilityPerTurn() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new QuintoriusKand(), new QuintoriusKand(),
                new OakenSiren(), new OakenSiren()));
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();
        Permanent locus = findPermanent(player1, "Locus of Enlightenment");
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(locus.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");
    }

    private Permanent craftWithSirens() {
        harness.addToBattlefield(player1, new TheEnigmaJewel());
        harness.setGraveyard(player1, List.of(new OakenSiren(), new OakenSiren(),
                new OakenSiren(), new OakenSiren()));
        addCraftMana();
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();
        return findPermanent(player1, "Locus of Enlightenment");
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
