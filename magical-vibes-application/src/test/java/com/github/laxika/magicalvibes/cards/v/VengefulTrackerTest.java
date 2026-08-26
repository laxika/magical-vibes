package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrossHopper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VengefulTracker.class, ImplementsOfSacrifice.class})
class VengefulTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who sacrifices an artifact")
    void opponentSacrificesArtifact() {
        harness.addToBattlefield(player1, new VengefulTracker());
        harness.addToBattlefield(player2, new ImplementsOfSacrifice());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Implements of Sacrifice");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals 2 damage when an opponent sacrifices an artifact token")
    void opponentSacrificesArtifactToken() {
        harness.addToBattlefield(player1, new VengefulTracker());
        Permanent token = addArtifactToken(player2);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.assertNotOnBattlefield(player2, token.getCard().getName());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({DrossHopper.class, GrizzlyBears.class})
    @DisplayName("Does not trigger when an opponent sacrifices a nonartifact creature")
    void opponentSacrificesNonartifact() {
        harness.addToBattlefield(player1, new VengefulTracker());
        Permanent hopper = harness.addToBattlefieldAndReturn(player2, new DrossHopper());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, hopper.getId());
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    private Permanent addArtifactToken(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Artifact Token");
        tokenCard.setType(CardType.ARTIFACT);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                "Sacrifice this token: Add {C}."
        ));
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }
}
