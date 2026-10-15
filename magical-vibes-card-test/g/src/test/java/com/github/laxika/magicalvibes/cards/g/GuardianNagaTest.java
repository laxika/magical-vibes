package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BanishingCoils;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianNaga.class, BanishingCoils.class, FountainOfYouth.class, GloriousAnthem.class,
        GrizzlyBears.class, Shock.class})
class GuardianNagaTest extends BaseCardTest {

    @Test
    void adventureExilesTargetArtifactAndAllowsTheCreatureToBeCastLater() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GuardianNaga card = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureExilesTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        GuardianNaga card = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianNaga()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    void preventsAllDamageToItDuringItsControllersTurn() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, naga.getId());

        assertThat(naga.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotPreventDamageToItDuringAnOpponentsTurn() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, naga.getId());

        assertThat(naga.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GuardianNaga card = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian Naga");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.findExiledCard(artifact.getCard().getId())).isNotNull();
    }

    @Test
    void adventureWithAnIllegalTargetGoesToGraveyardWithoutCastPermission() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GuardianNaga card = new GuardianNaga();
        GuardianNaga response = new GuardianNaga();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(response));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, artifact.getId());
        harness.castAdventure(player2, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guardian Naga");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.findExiledCard(response.getId())).isNotNull();
    }

    @Test
    void creatureCanBeCastDirectlyWithoutUsingAdventure() {
        harness.castFromHand(player1, new GuardianNaga(), "{5}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian Naga");
        harness.assertNotInGraveyard(player1, "Guardian Naga");
    }

    @Test
    void damagePreventionDoesNotProtectOtherCreatures() {
        addCreatureReady(player1, new GuardianNaga());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Guardian Naga");
    }

    @Test
    void vigilanceKeepsItUntappedAfterAttacking() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());

        declareAttackers(List.of(0));

        assertThat(naga.isTapped()).isFalse();
    }

    @Test
    void preventsCombatDamageWhileAttacking() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(naga.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotPreventCombatDamageWhileBlockingOnAnOpponentsTurn() {
        Permanent naga = addCreatureReady(player1, new GuardianNaga());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(naga.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
