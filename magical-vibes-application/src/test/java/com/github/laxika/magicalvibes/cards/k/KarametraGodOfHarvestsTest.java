package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CanopyVista;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FatedRetribution;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarametraGodOfHarvests.class, Forest.class, Plains.class, GrizzlyBears.class, GlorySeeker.class,
        NyxbornShieldmate.class, NyxbornWolf.class, RevokeExistence.class, CanopyVista.class,
        FatedRetribution.class, MycosynthLattice.class})
class KarametraGodOfHarvestsTest extends BaseCardTest {

    @Test
    @DisplayName("Karametra is not a creature below seven combined green and white devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent karametra = addKarametra();
        addGreenAndWhitePermanents(4);

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
        assertThat(gqs.isEnchantment(gd, karametra)).isTrue();
    }

    @Test
    @DisplayName("Karametra becomes a creature at seven combined green and white devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent karametra = addKarametra();
        addGreenAndWhitePermanents(5);

        assertThat(gqs.isCreature(gd, karametra)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature may search for a Forest or Plains and put it onto the battlefield tapped")
    void creatureSpellMaySearchForForestOrPlains() {
        addKarametra();
        Forest forest = new Forest();
        Plains plains = new Plains();
        setLibrary(forest, plains, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);

        harness.handleCardChosen(player1, 0);

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest || permanent.getCard() == plains)
                .findFirst()
                .orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the creature spell trigger does not search")
    void decliningSearchDoesNothing() {
        addKarametra();
        setLibrary(new Forest(), new Plains());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest || permanent.getCard() instanceof Plains);
    }

    @Test
    void losesCreatureTypeWhenDevotionDropsBelowSeven() {
        Permanent karametra = addKarametra();
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new NyxbornShieldmate());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new NyxbornWolf());
        }
        assertThat(gqs.isCreature(gd, karametra)).isTrue();
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, shieldmate.getId());

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
        assertThat(gqs.isEnchantment(gd, karametra)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karametra).doesNotContain(shieldmate);
    }

    @Test
    void opponentsPermanentsDoNotContributeDevotion() {
        Permanent karametra = addKarametra();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new NyxbornWolf());
        }

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
    }

    @Test
    void bestowCastDoesNotTriggerSearch() {
        addKarametra();
        Permanent host = harness.addToBattlefieldAndReturn(player1, new NyxbornShieldmate());
        setLibrary(new Forest());
        harness.setHand(player1, List.of(new NyxbornWolf()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void noncreatureSpellDoesNotTriggerSearch() {
        addKarametra();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());
        setLibrary(new Forest());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void canFailToFindEvenWhenEligibleLandExists() {
        addKarametra();
        Forest forest = new Forest();
        setLibrary(forest);
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nyxborn Shieldmate");
    }

    @Test
    void emptyLibraryDoesNotPreventCreatureSpellFromResolving() {
        addKarametra();
        setLibrary();
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nyxborn Shieldmate");
    }

    @Test
    void nonbasicForestPlainsIsEligibleAndSearchResolvesBeforeCreature() {
        addKarametra();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        CanopyVista garden = new CanopyVista();
        setLibrary(garden);
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(garden);
        harness.assertNotOnBattlefield(player1, "Nyxborn Shieldmate");
        harness.handleCardChosen(player1, 0);
        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == garden).findFirst().orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nyxborn Shieldmate");
    }

    @Test
    void indestructibleKarametraSurvivesCreatureDestructionAndLosesDevotion() {
        Permanent karametra = addKarametra();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new NyxbornShieldmate());
        }
        assertThat(gqs.isCreature(gd, karametra)).isTrue();
        harness.forceActivePlayer(player2);

        harness.castFromHand(player1, new FatedRetribution(), "{4}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(karametra);
        assertThat(gqs.isCreature(gd, karametra)).isFalse();
        assertThat(gqs.isEnchantment(gd, karametra)).isTrue();
    }

    @Test
    void losingCreatureTypePreservesArtifactTypeGrantedByOlderLattice() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent karametra = addKarametra();

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
        assertThat(gqs.isEnchantment(gd, karametra)).isTrue();
        assertThat(gqs.isArtifact(gd, karametra)).isTrue();
    }

    private Permanent addKarametra() {
        return harness.addToBattlefieldAndReturn(player1, new KarametraGodOfHarvests());
    }

    private void addGreenAndWhitePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, i % 2 == 0 ? new GrizzlyBears() : new GlorySeeker());
        }
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
