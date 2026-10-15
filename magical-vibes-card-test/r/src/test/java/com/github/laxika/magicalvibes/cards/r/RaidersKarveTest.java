package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaidersKarve.class, RavenousLindwurm.class, AxgardCavalry.class, Forest.class})
class RaidersKarveTest extends BaseCardTest {

    @Test
    void crewingAnimatesRaidersKarveAndAttackingOffersTopLand() {
        Permanent karve = addRaidersKarveReady();
        Permanent crew = addCreatureReady(player1, new RavenousLindwurm());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(karve.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(topLand);
        assertThat(land).isNotNull();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void decliningTopLandLeavesItOnTopOfLibrary() {
        addRaidersKarveReady();
        addCreatureReady(player1, new RavenousLindwurm());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
        assertThat(findPermanent(topLand)).isNull();
    }

    @Test
    void nonlandTopCardStaysOnTopWithoutChoice() {
        addRaidersKarveReady();
        addCreatureReady(player1, new RavenousLindwurm());
        AxgardCavalry topCard = new AxgardCavalry();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void attackingWithEmptyLibraryDoesNotOfferChoice() {
        addRaidersKarveReady();
        addCreatureReady(player1, new RavenousLindwurm());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void summoningSickCreaturesCanCombinePowerToCrew() {
        Permanent karve = addRaidersKarveReady();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, karve)).isTrue();
        assertThat(karve.isTapped()).isFalse();
    }

    @Test
    void insufficientCrewPowerCannotActivate() {
        Permanent karve = addRaidersKarveReady();
        Permanent crew = addCreatureReady(player1, new AxgardCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, karve)).isFalse();
    }

    @Test
    void crewAnimationExpiresAfterTurn() {
        Permanent karve = addRaidersKarveReady();
        addCreatureReady(player1, new RavenousLindwurm());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, karve)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, karve)).isFalse();
    }

    private Permanent addRaidersKarveReady() {
        return addCreatureReady(player1, new RaidersKarve());
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
