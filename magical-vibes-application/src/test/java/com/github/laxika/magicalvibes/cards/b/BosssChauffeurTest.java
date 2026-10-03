package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BosssChauffeur.class, GrizzlyBears.class, WrathOfGod.class})
class BosssChauffeurTest extends BaseCardTest {

    @Test
    void enteringAloneGetsOneCounterAndDoesNotTriggerItsOwnAlliance() {
        Permanent chauffeur = castChauffeur();

        assertThat(chauffeur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Boss's Chauffeur");
    }

    @Test
    void allianceTriggersForEachCreatureWithoutOncePerTurnLimit() {
        Permanent chauffeur = castChauffeur();

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.COLORLESS);
        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.COLORLESS);

        assertThat(chauffeur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void simultaneousDeathsCreateTokensForEachChauffeursController() {
        Permanent ownChauffeur = harness.addToBattlefieldAndReturn(player1, new BosssChauffeur());
        ownChauffeur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingChauffeur = harness.addToBattlefieldAndReturn(player2, new BosssChauffeur());
        opposingChauffeur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
        assertThat(findPermanents(player2, "Citizen")).hasSize(4);
        harness.assertNotOnBattlefield(player1, "Boss's Chauffeur");
        harness.assertNotOnBattlefield(player2, "Boss's Chauffeur");
    }

    @Test
    void entersWithOneCounterPlusOneForEachOtherControlledCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent chauffeur = castChauffeur();

        assertThat(chauffeur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void alliancePutsCounterOnChauffeurForAnotherCreatureEntering() {
        Permanent chauffeur = castChauffeur();

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.GREEN);

        assertThat(chauffeur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void deathCreatesCitizenForEachPlusOneCounter() {
        Permanent chauffeur = harness.addToBattlefieldAndReturn(player1, new BosssChauffeur());
        chauffeur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        chauffeur.setCounterCount(CounterType.CHARGE, 1);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(3);
        assertThat(citizens).allSatisfy(citizen -> {
            assertThat(citizen.getCard().getPower()).isEqualTo(1);
            assertThat(citizen.getCard().getToughness()).isEqualTo(1);
            assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(citizen.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        });
    }

    private Permanent castChauffeur() {
        castCreature(new BosssChauffeur(), ManaColor.WHITE, ManaColor.COLORLESS, ManaColor.COLORLESS,
                ManaColor.COLORLESS, ManaColor.COLORLESS);
        return findPermanent(player1, "Boss's Chauffeur");
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Card creature, ManaColor... mana) {
        harness.setHand(player1, List.of(creature));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
