package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarthaJones.class, GrizzlyBears.class})
class MarthaJonesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and investigates")
    void entersAndInvestigates() {
        harness.setHand(player1, List.of(new MarthaJones()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Clue makes Martha and another creature unblockable")
    void clueSacrificeMakesMarthaAndTargetUnblockable() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent clue = addClueToken(player1);

        sacrificeClue(clue, target);

        assertThat(martha.isCantBeBlocked()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Clue makes Martha unblockable without a target")
    void clueSacrificeWorksWithoutOtherCreature() {
        Permanent martha = addCreatureReady(player1, new MarthaJones());
        Permanent clue = addClueToken(player1);

        sacrificeClue(clue, null);

        assertThat(martha.isCantBeBlocked()).isTrue();
    }

    private void sacrificeClue(Permanent clue, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.passBothPriorities();
        if (target != null && gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        resolveAllTriggers();
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
        return clue;
    }
}
