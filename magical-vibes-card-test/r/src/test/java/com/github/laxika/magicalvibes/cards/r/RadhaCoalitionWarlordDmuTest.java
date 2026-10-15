package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.s.SunlitMarsh;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadhaCoalitionWarlord.class, GrizzlyBears.class, Forest.class, Island.class, Mountain.class,
        NishobaBrawler.class, SunlitMarsh.class})
class RadhaCoalitionWarlordDmuTest extends BaseCardTest {

    @Test
    @DisplayName("When Radha becomes tapped, another creature gets a Domain boost")
    void tappingRadhaBoostsAnotherCreatureByDomain() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        tapAndQueueTrigger(radha);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Radha")
    void tappingAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        tapAndQueueTrigger(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Domain is determined on resolution and the resolved boost stays fixed")
    void domainIsCountedOnResolution() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new NishobaBrawler());
        harness.addToBattlefield(player1, new Forest());

        tapAndQueueTrigger(radha);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);

        harness.addToBattlefield(player1, new Mountain());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonbasic land contributes each of its basic land types")
    void countsBasicLandTypesOnNonbasicLands() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new NishobaBrawler());
        harness.addToBattlefield(player1, new SunlitMarsh());
        harness.addToBattlefield(player1, new Forest());

        tapAndQueueTrigger(radha);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Radha still targets another creature when domain is zero")
    void zeroDomainStillRequiresTarget() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new NishobaBrawler());

        tapAndQueueTrigger(radha);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Radha's ability resolves after Radha leaves the battlefield")
    void abilityResolvesWithoutRadha() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new NishobaBrawler());
        harness.addToBattlefield(player1, new Forest());

        tapAndQueueTrigger(radha);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(radha);
        gd.playerGraveyards.get(player1.getId()).add(radha.getCard());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declaring Radha as an attacker triggers her domain ability")
    void attackingTriggersDomainBoost() {
        Permanent radha = addCreatureReady(player1, new RadhaCoalitionWarlord());
        Permanent target = addCreatureReady(player1, new NishobaBrawler());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(radha.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
    }
}
