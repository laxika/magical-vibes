package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionOfStraySouls.class, NyxbornRollicker.class, SpringleafDrum.class})
class ChampionOfStraySoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices X other creatures and returns X targeted creature cards")
    void sacrificesOtherCreaturesAndReturnsTargets() {
        Permanent champion = addReadyChampion();
        Permanent firstSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Permanent secondSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Card firstTarget = new NyxbornRollicker();
        Card secondTarget = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        addManaForFirstAbility();

        gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null, Zone.GRAVEYARD,
                List.of(firstTarget.getId(), secondTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstSacrifice, secondSacrifice);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstTarget.getId(), secondTarget.getId());
    }

    @Test
    @DisplayName("The source creature cannot be paid as one of the other creatures")
    void sourceCannotBeSacrificedAsOtherCreature() {
        Permanent champion = addReadyChampion();
        Permanent otherCreature = addCreatureReady(player1, new NyxbornRollicker());
        Card firstTarget = new NyxbornRollicker();
        Card secondTarget = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null,
                Zone.GRAVEYARD, List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(champion, otherCreature);
    }

    @Test
    @DisplayName("The graveyard ability puts this card on top of its owner's library")
    void returnsItselfToTopOfLibrary() {
        Card champion = new ChampionOfStraySouls();
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(champion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(champion);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(champion);
    }

    @Test
    void cannotChooseMoreSacrificesThanAvailableGraveyardTargets() {
        Permanent champion = addReadyChampion();
        Permanent firstSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Permanent secondSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Card target = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(target));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null,
                Zone.GRAVEYARD, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(champion, firstSacrifice, secondSacrifice);
        assertThat(champion.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    void canActivateWithZeroSacrificesAndZeroTargets() {
        Permanent champion = addReadyChampion();
        harness.setGraveyard(player1, List.of());
        addManaForFirstAbility();

        harness.activateAbilityWithMultiTargets(player1, indexOf(champion), 0, 0, List.of());
        assertThat(champion.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(champion);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetTheCreatureBeingSacrificed() {
        Permanent champion = addReadyChampion();
        Permanent sacrifice = addCreatureReady(player1, new NyxbornRollicker());
        harness.setGraveyard(player1, List.of(new NyxbornRollicker()));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 1, null,
                Zone.GRAVEYARD, List.of(sacrifice.getCard().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(champion, sacrifice);
    }

    @Test
    void cannotTargetAnOpponentsGraveyard() {
        Permanent champion = addReadyChampion();
        addCreatureReady(player1, new NyxbornRollicker());
        Card opponentTarget = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(new NyxbornRollicker()));
        harness.setGraveyard(player2, List.of(opponentTarget));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 1, null,
                Zone.GRAVEYARD, List.of(opponentTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTheSameCardTwice() {
        Permanent champion = addReadyChampion();
        addCreatureReady(player1, new NyxbornRollicker());
        addCreatureReady(player1, new NyxbornRollicker());
        Card target = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(target, new NyxbornRollicker()));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null,
                Zone.GRAVEYARD, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherTargetLeavesGraveyard() {
        Permanent champion = addReadyChampion();
        Permanent firstSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Permanent secondSacrifice = addCreatureReady(player1, new NyxbornRollicker());
        Card removedTarget = new NyxbornRollicker();
        Card remainingTarget = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(removedTarget, remainingTarget));
        addManaForFirstAbility();

        gs.activateAbility(gd, player1, indexOf(champion), 0, 2, null, Zone.GRAVEYARD,
                List.of(removedTarget.getId(), remainingTarget.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(removedTarget);
        harness.setExile(player1, List.of(removedTarget));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(remainingTarget.getId()).doesNotContain(removedTarget.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstSacrifice.getCard(), secondSacrifice.getCard())
                .doesNotContain(remainingTarget);
    }

    @Test
    void summoningSicknessPreventsTheTapAbility() {
        Permanent champion = addReadyChampion();
        champion.setSummoningSick(true);
        addCreatureReady(player1, new NyxbornRollicker());
        Card target = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(target));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 1, null,
                Zone.GRAVEYARD, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardAbilityReturnsOnlyTheActivatedCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        Card otherChampion = new ChampionOfStraySouls();
        Card champion = new ChampionOfStraySouls();
        harness.setGraveyard(player1, List.of(otherChampion, champion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherChampion);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(champion);
    }

    @Test
    void graveyardAbilityDoesNothingIfItsSourceLeavesTheGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        Card champion = new ChampionOfStraySouls();
        Card otherChampion = new ChampionOfStraySouls();
        harness.setGraveyard(player1, List.of(champion, otherChampion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0, 0);
        gd.playerGraveyards.get(player1.getId()).remove(champion);
        harness.setExile(player1, List.of(champion));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherChampion);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(champion, otherChampion);
    }

    @Test
    void cannotTargetANoncreatureCard() {
        Permanent champion = addReadyChampion();
        addCreatureReady(player1, new NyxbornRollicker());
        Card artifact = new SpringleafDrum();
        harness.setGraveyard(player1, List.of(artifact, new NyxbornRollicker()));
        addManaForFirstAbility();

        assertThatThrownBy(() -> gs.activateAbility(gd, player1, indexOf(champion), 0, 1, null,
                Zone.GRAVEYARD, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseWhichOtherCreatureToSacrifice() {
        Permanent champion = addReadyChampion();
        Permanent chosen = addCreatureReady(player1, new NyxbornRollicker());
        Permanent retained = addCreatureReady(player1, new NyxbornRollicker());
        Card target = new NyxbornRollicker();
        harness.setGraveyard(player1, List.of(target));
        addManaForFirstAbility();

        gs.activateAbility(gd, player1, indexOf(champion), 0, 1, null,
                Zone.GRAVEYARD, List.of(target.getId()));
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(champion, retained).doesNotContain(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).contains(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chosen.getCard());
    }

    private Permanent addReadyChampion() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return addCreatureReady(player1, new ChampionOfStraySouls());
    }

    private void addManaForFirstAbility() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
