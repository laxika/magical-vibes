package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PillarfieldOx;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValakutTheMoltenPinnacle.class, Mountain.class, Plains.class, PillarfieldOx.class, PrismaticOmen.class})
class ValakutTheMoltenPinnacleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ValakutTheMoltenPinnacle()));

        harness.playLand(player1, 0);

        Permanent valakut = findPermanent(player1, "Valakut, the Molten Pinnacle");
        assertThat(valakut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A sixth Mountain triggers the optional three damage")
    void sixthMountainTriggersDamage() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The fifth Mountain is not enough other Mountains")
    void fifthMountainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(4);
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tapForRedMana() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent valakut = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(valakut.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void mayDeclineDamage() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDamageCreature() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        Permanent ox = harness.addToBattlefieldAndReturn(player2, new PillarfieldOx());
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, ox.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ox.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Pillarfield Ox");
    }

    @Test
    void nonMountainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsMountainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);

        harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void losingOtherMountainStopsDamage() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        Permanent otherMountain = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(otherMountain);
        gd.playerGraveyards.get(player1.getId()).add(otherMountain.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void losingTriggeringMountainDoesNotStopDamage() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(5);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent enteringMountain = gd.playerBattlefields.get(player1.getId()).getLast();
        gd.playerBattlefields.get(player1.getId()).remove(enteringMountain);
        gd.playerGraveyards.get(player1.getId()).add(enteringMountain.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
    }

    @Test
    void valakutCountsAsOtherMountainWithPrismaticOmen() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        addMountains(4);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 17);
    }

    @Test
    void valakutTriggersForItsOwnEntryWhenItIsMountain() {
        harness.addToBattlefield(player1, new PrismaticOmen());
        addMountains(5);
        harness.setHand(player1, List.of(new ValakutTheMoltenPinnacle()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 17);
    }

    private void addMountains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
    }
}
