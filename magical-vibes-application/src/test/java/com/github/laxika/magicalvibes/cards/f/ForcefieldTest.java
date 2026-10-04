package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.w.WarMammoth;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forcefield.class, GrizzlyBears.class, ProdigalSorcerer.class, WarMammoth.class})
class ForcefieldTest extends BaseCardTest {

    @Test
    void preventsAllButOneDamageFromChosenUnblockedCreature() {
        harness.setLife(player1, 20);
        Permanent forcefield = addReadyPermanent(player1, new Forcefield());
        Permanent chosenAttacker = addReadyCreature(player2, 5);
        addReadyCreature(player2, 2);

        activateAndChooseSource(forcefield, chosenAttacker);

        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void doesNotPreventNoncombatDamageFromChosenCreature() {
        harness.setLife(player1, 20);
        Permanent forcefield = addReadyPermanent(player1, new Forcefield());
        Permanent sorcerer = addReadyPermanent(player2, new ProdigalSorcerer());

        activateAndChooseSource(forcefield, sorcerer);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void doesNotPreventTrampleDamageFromBlockedCreature() {
        harness.setLife(player1, 20);
        Permanent forcefield = addReadyPermanent(player1, new Forcefield());
        Permanent mammoth = addReadyPermanent(player2, new WarMammoth());
        addReadyPermanent(player1, new ProdigalSorcerer());

        activateAndChooseSource(forcefield, mammoth);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void repeatedActivationsStillLeaveOneDamage() {
        harness.setLife(player1, 20);
        Permanent forcefield = addReadyPermanent(player1, new Forcefield());
        Permanent mammoth = addReadyPermanent(player2, new WarMammoth());

        activateAndChooseSource(forcefield, mammoth);
        activateAndChooseSource(forcefield, mammoth);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void doesNotPreventDamageToTheOtherPlayer() {
        harness.setLife(player2, 20);
        Permanent forcefield = addReadyPermanent(player1, new Forcefield());
        Permanent mammoth = addReadyPermanent(player1, new WarMammoth());

        activateAndChooseSource(forcefield, mammoth);

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void activateAndChooseSource(Permanent forcefield, Permanent source) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, forcefield), null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(source.getId()).doesNotContain(forcefield.getId());
        harness.handlePermanentChosen(player1, source.getId());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, int power) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        return addReadyPermanent(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
