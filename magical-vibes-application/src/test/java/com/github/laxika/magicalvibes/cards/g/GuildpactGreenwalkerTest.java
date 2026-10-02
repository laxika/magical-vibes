package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzoriusArrester;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildpactGreenwalker.class, GrizzlyBears.class, AzoriusArrester.class, Shock.class})
class GuildpactGreenwalkerTest extends BaseCardTest {

    @Test
    void mayIncorporateAndPerpetuallyBoostAChosenCreatureCard() {
        GuildpactGreenwalker greenwalker = new GuildpactGreenwalker();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(greenwalker, bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent chosen = findPermanent(player1, "Grizzly Bears");
        assertThat(chosen.getEffectivePower()).isEqualTo(6);
        assertThat(chosen.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void multicoloredCreaturesYouControlHaveWardTwo() {
        harness.addToBattlefield(player1, new GuildpactGreenwalker());
        Permanent arrester = addReadyCreature(player1, new AzoriusArrester());

        castShockAt(player2, arrester);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arrester);
    }

    @Test
    void monocoloredCreaturesDoNotHaveWard() {
        harness.addToBattlefield(player1, new GuildpactGreenwalker());
        addReadyCreature(player1, new GrizzlyBears());

        castShockAt(player2, findPermanent(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castShockAt(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
    }
}
