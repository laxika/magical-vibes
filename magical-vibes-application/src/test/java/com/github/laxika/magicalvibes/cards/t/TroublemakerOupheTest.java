package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.h.HopefulVigil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroublemakerOuphe.class, CandyTrail.class, HopefulVigil.class})
class TroublemakerOupheTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotExileAnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Candy Trail");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals(target.getCard().getName()));
    }

    @Test
    void withBargainExilesAnArtifactAnOpponentControls() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Candy Trail");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Candy Trail"));
        harness.assertInGraveyard(player1, "Candy Trail");
    }

    @Test
    void withBargainExilesAnEnchantmentAnOpponentControls() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HopefulVigil());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hopeful Vigil");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hopeful Vigil"));
    }

    @Test
    void bargainCannotTargetOwnArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotTargetCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TroublemakerOuphe());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainedCreatureChoosesTargetAfterEntering() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Troublemaker Ouphe");
        harness.assertNotOnBattlefield(player2, "Candy Trail");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void canBargainWithoutAnyLegalExileTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.assertInGraveyard(player1, "Candy Trail");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Troublemaker Ouphe");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canSacrificeAnEnchantmentToBargain() {
        harness.setLibrary(player1, List.of());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new HopefulVigil());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.assertInGraveyard(player1, "Hopeful Vigil");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Troublemaker Ouphe");
    }

    @Test
    void canSacrificeANonartifactNonenchantmentCreatureTokenToBargain() {
        harness.setHand(player1, List.of(new HopefulVigil(), new TroublemakerOuphe()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, token.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(token.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Candy Trail");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void cannotSacrificeANontokenCreatureToBargain() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new TroublemakerOuphe());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Troublemaker Ouphe");
        harness.assertNotInGraveyard(player1, "Troublemaker Ouphe");
    }

    @Test
    void cannotSacrificeAnOpponentsArtifactToBargain() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new TroublemakerOuphe()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Candy Trail");
        harness.assertNotInGraveyard(player2, "Candy Trail");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
