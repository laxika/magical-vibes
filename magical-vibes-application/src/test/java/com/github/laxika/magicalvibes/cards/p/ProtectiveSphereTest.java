package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProtectiveSphereTest extends BaseCardTest {

    @Test
    void coloredManaRestrictsSourceChoice() {
        addReadySphere();
        Permanent redSource = addReadyCreature(player2, new FireElemental());
        Permanent greenSource = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(redSource.getId()).doesNotContain(greenSource.getId());

        harness.handlePermanentChosen(player1, redSource.getId());

        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId())).contains(redSource.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(redSource.getId());
    }

    @Test
    void colorlessManaDoesNotAllowAChoice() {
        addReadySphere();
        addReadyCreature(player2, new FireElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerSourceDamagePreventionIds).doesNotContainKey(player1.getId());
    }

    private Permanent addReadySphere() {
        ProtectiveSphere card = new ProtectiveSphere();
        Permanent sphere = new Permanent(card);
        sphere.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sphere);
        return sphere;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
