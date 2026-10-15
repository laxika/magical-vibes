package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.b.BrotherhoodRegalia;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatonhnhakTon.class, BrotherhoodRegalia.class, AnointedProcession.class})
class RatonhnhakTonTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof and can't be blocked before dealing damage")
    void hasInitialProtectionAndEvasion() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());

        assertThat(gqs.hasKeyword(gd, ratonhnhak, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, ratonhnhak)).isTrue();
    }

    @Test
    @DisplayName("Loses hexproof and can't-be-blocked after dealing damage")
    void losesInitialAbilitiesAfterDealingDamage() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());
        ratonhnhak.setAttacking(true);

        resolveCombat();

        assertThat(gqs.hasKeyword(gd, ratonhnhak, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, ratonhnhak)).isFalse();
    }

    @Test
    @DisplayName("Creates a menacing Assassin and returns an Equipment attached to it")
    void createsAssassinAndReturnsEquipmentAttachedToIt() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());
        ratonhnhak.setAttacking(true);
        BrotherhoodRegalia equipment = new BrotherhoodRegalia();
        harness.setGraveyard(player1, List.of(equipment));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(findPermanents(player1, "Assassin")).hasSize(1);
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        resolveAllTriggers();

        Permanent assassin = findPermanent(player1, "Assassin");
        Permanent returnedEquipment = findPermanent(player1, "Brotherhood Regalia");

        assertThat(assassin.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(returnedEquipment.getAttachedTo()).isEqualTo(assassin.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void createsAssassinWithoutEquipmentInGraveyard() {
        addCreatureReady(player1, new RatonhnhakTon()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotReturnEquipmentFromOpponentsGraveyard() {
        addCreatureReady(player1, new RatonhnhakTon()).setAttacking(true);
        BrotherhoodRegalia equipment = new BrotherhoodRegalia();
        harness.setGraveyard(player2, List.of(equipment));

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Assassin")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(equipment);
        harness.assertNotOnBattlefield(player1, "Brotherhood Regalia");
    }

    @Test
    void returnsEquipmentUnattachedIfAssassinLeavesBeforeReflexiveTriggerResolves() {
        addCreatureReady(player1, new RatonhnhakTon()).setAttacking(true);
        BrotherhoodRegalia equipment = new BrotherhoodRegalia();
        harness.setGraveyard(player1, List.of(equipment));

        resolveCombat();
        resolveAllTriggers();
        Permanent assassin = findPermanent(player1, "Assassin");
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, assassin));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Brotherhood Regalia").getAttachedTo()).isNull();
        harness.assertNotInGraveyard(player1, "Brotherhood Regalia");
    }

    @Test
    void doesNotRegainProtectionOnLaterTurns() {
        Permanent ratonhnhak = addCreatureReady(player1, new RatonhnhakTon());
        ratonhnhak.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        advanceToUpkeep(player2);

        assertThat(gqs.hasKeyword(gd, ratonhnhak, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, ratonhnhak)).isFalse();
    }

    @Test
    void doubledAssassinsEachTriggerAnEquipmentReturn() {
        harness.addToBattlefield(player1, new AnointedProcession());
        addCreatureReady(player1, new RatonhnhakTon()).setAttacking(true);
        BrotherhoodRegalia firstEquipment = new BrotherhoodRegalia();
        BrotherhoodRegalia secondEquipment = new BrotherhoodRegalia();
        harness.setGraveyard(player1, List.of(firstEquipment, secondEquipment));

        resolveCombat();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Assassin")).hasSize(2);
        harness.handleMultipleCardsChosen(player1, List.of(firstEquipment.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondEquipment.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Brotherhood Regalia")).hasSize(2);
        assertThat(findPermanents(player1, "Brotherhood Regalia"))
                .extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrderElementsOf(findPermanents(player1, "Assassin").stream()
                        .map(Permanent::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
