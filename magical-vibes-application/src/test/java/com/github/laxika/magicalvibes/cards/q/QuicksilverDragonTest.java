package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.c.ChokingTethers;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverDragon.class, Shock.class, GlorySeeker.class, AphettoAlchemist.class,
        ChokingTethers.class})
class QuicksilverDragonTest extends BaseCardTest {

    @Test
    void redirectsSpellTargetingQuicksilverDragonToAnotherCreature() {
        Permanent dragon = castFaceUpDragon();
        Permanent replacementCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragon.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(replacementCreature.getId())
                .doesNotContain(dragon.getId());

        harness.handlePermanentChosen(player2, replacementCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    void doesNothingWhenSpellDoesNotTargetQuicksilverDragon() {
        Permanent dragon = castFaceUpDragon();
        Permanent replacementCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, replacementCreature.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    void doesNothingWhenTargetSpellHasMultipleTargets() {
        Permanent dragon = castFaceUpDragon();
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent alternateTarget = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        ChokingTethers tethers = new ChokingTethers();
        harness.setHand(player1, List.of(tethers));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(dragon.getId(), firstTarget.getId()));
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, tethers.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isTrue();
        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(alternateTarget.isTapped()).isFalse();
    }

    @Test
    void doesNothingWhenTargetSpellHasNoTargets() {
        Permanent dragon = castFaceUpDragon();
        GlorySeeker creatureSpell = new GlorySeeker();
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, creatureSpell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glory Seeker");
    }

    @Test
    void targetsSpellsButNotAbilities() {
        Permanent dragon = castFaceUpDragon();
        Permanent alchemist = addCreatureReady(player1, new AphettoAlchemist());

        harness.activateAbility(player1, battlefieldIndex(player1, alchemist), null, dragon.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, battlefieldIndex(player2, dragon), null, alchemist.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    private Permanent castFaceUpDragon() {
        harness.setHand(player2, List.of(new QuicksilverDragon()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player2, "Quicksilver Dragon");
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.turnFaceUp(player2, battlefieldIndex(player2, dragon));
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return dragon;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
