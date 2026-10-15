package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeOfTheParty.class, SakuraTribeElder.class, BeastWithin.class, DressDown.class})
class LifeOfThePartyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts Life of the Party by the number of creatures you control")
    void attackBoostCountsControlledCreatures() {
        Permanent lifeOfTheParty = addCreatureReady(player1, new LifeOfTheParty());
        addCreatureReady(player1, new SakuraTribeElder());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(lifeOfTheParty.getPowerModifier()).isEqualTo(2);
        assertThat(lifeOfTheParty.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB gives each opponent a goaded token copy")
    void etbCreatesGoadedTokenCopyForOpponent() {
        Permanent lifeOfTheParty = castLifeOfTheParty();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1)
                .first()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(als.getMustAttackRequirementCount(gd, token)).isEqualTo(1);
                });
        assertThat(als.getMustAttackRequirementCount(gd, lifeOfTheParty)).isZero();
    }

    @Test
    @DisplayName("Token copies do not retrigger Life of the Party's ETB")
    void tokenCopyDoesNotRetriggerEtb() {
        castLifeOfTheParty();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attack boost counts creatures when the trigger resolves")
    void attackBoostUsesResolutionTimeCount() {
        Permanent source = addCreatureReady(player1, new LifeOfTheParty());
        addCreatureReady(player1, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        addCreatureReady(player1, new SakuraTribeElder());
        resolveAllTriggers();

        assertThat(source.getPowerModifier()).isEqualTo(3);
        assertThat(source.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing the original before its enter trigger resolves still creates copies")
    void copiesUseLastKnownInformation() {
        castLifeOfThePartyWithPendingTrigger();
        Permanent source = findPermanent(player1, "Life of the Party");
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Life of the Party");
        assertThat(findPermanents(player2, "Life of the Party")).hasSize(1);
        assertThat(als.getMustAttackRequirementCount(gd,
                findPermanent(player2, "Life of the Party"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Losing abilities does not remove permanent goad")
    void tokenRemainsGoadedWithoutAbilities() {
        castLifeOfTheParty();
        Permanent token = findPermanent(player2, "Life of the Party");
        harness.addToBattlefield(player1, new DressDown());

        assertThat(als.getMustAttackRequirementCount(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent token attack boost counts that opponent's creatures")
    void tokenAttackCountsItsControllersCreatures() {
        castLifeOfTheParty();
        Permanent token = findPermanent(player2, "Life of the Party");
        addCreatureReady(player2, new SakuraTribeElder());
        addCreatureReady(player2, new SakuraTribeElder());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(token.getPowerModifier()).isEqualTo(3);
        assertThat(token.getToughnessModifier()).isZero();
    }

    private void castLifeOfThePartyWithPendingTrigger() {
        harness.setHand(player1, List.of(new LifeOfTheParty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private Permanent castLifeOfTheParty() {
        castLifeOfThePartyWithPendingTrigger();
        resolveAllTriggers();
        return findPermanent(player1, "Life of the Party");
    }
}
