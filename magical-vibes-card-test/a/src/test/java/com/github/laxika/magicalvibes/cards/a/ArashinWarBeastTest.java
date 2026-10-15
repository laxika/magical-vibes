package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArashinWarBeast.class, FrontierMastodon.class})
class ArashinWarBeastTest extends BaseCardTest {

    @Test
    void attackingAndDamagingABlockerManifestsTheTopCard() {
        Permanent beast = addCreatureReady(player1, new ArashinWarBeast());
        beast.setAttacking(true);
        addCreatureReady(player2, new FrontierMastodon());
        Card topCard = new FrontierMastodon();
        harness.setLibrary(player1, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    void multipleDamagedBlockersCauseOnlyOneManifestTrigger() {
        Permanent beast = addCreatureReady(player1, new ArashinWarBeast());
        beast.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new FrontierMastodon());
        Permanent blocker2 = addCreatureReady(player2, new FrontierMastodon());
        harness.setLibrary(player1, List.of(new FrontierMastodon()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 3,
                blocker2.getId(), 3));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void damagingAnAttackerWhileBlockingDoesNotTriggerManifest() {
        Permanent attacker = addCreatureReady(player1, new FrontierMastodon());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ArashinWarBeast());
        harness.setLibrary(player2, List.of(new FrontierMastodon()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void unblockedCombatDamageDoesNotManifest() {
        addCreatureReady(player1, new ArashinWarBeast()).setAttacking(true);
        Card topCard = new ArashinWarBeast();
        harness.setLibrary(player1, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    void emptyLibraryDoesNotCreateAManifestedPermanent() {
        addCreatureReady(player1, new ArashinWarBeast()).setAttacking(true);
        addCreatureReady(player2, new ArashinWarBeast());
        harness.setLibrary(player1, List.of());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void lethalCombatStillManifestsAndTheCreatureCanTurnFaceUp() {
        addCreatureReady(player1, new ArashinWarBeast()).setAttacking(true);
        addCreatureReady(player2, new ArashinWarBeast());
        Card topCard = new ArashinWarBeast();
        harness.setLibrary(player1, List.of(topCard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(6);
    }
}
