package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.cards.l.LysAlanaScarblade;
import com.github.laxika.magicalvibes.cards.r.RendingVines;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshesOfTheFallen.class, ArabaMothrider.class, ElvishEulogist.class, LysAlanaScarblade.class,
        RendingVines.class})
class AshesOfTheFallenTest extends BaseCardTest {

    @Test
    void chosenTypeAppliesToCreatureCardsInYourGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new ArabaMothrider()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void chosenTypeDoesNotApplyToCreatureCardsInHand() {
        Permanent scarblade = addCreatureReady(player1, new LysAlanaScarblade());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishEulogist());
        harness.setHand(player1, List.of(new AshesOfTheFallen(), new ArabaMothrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        int scarbladeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scarblade);
        assertThatThrownBy(() -> harness.activateAbility(player1, scarbladeIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chosenTypeDoesNotApplyToAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player2, List.of(new ArabaMothrider()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void chosenTypeDoesNotApplyToNoncreatureCardsInYourGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new AshesOfTheFallen()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void choosingAnotherTypePreservesExistingCreatureTypes() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new ElvishEulogist(), new ArabaMothrider()));
        harness.castFromHand(player1, new AshesOfTheFallen(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.SAMURAI.name());

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void multipleCopiesAddTheirTypesWithoutReplacingEachOther() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new ArabaMothrider()));
        castAndChooseElf();
        harness.castFromHand(player1, new AshesOfTheFallen(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.SAMURAI.name());

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void destroyingAshesBeforeResolutionRemovesTheGrantedType() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new ArabaMothrider()));
        castAndChooseElf();
        Permanent ashes = findPermanent(player1, "Ashes of the Fallen");
        harness.setHand(player2, List.of(new RendingVines(), new ArabaMothrider(), new ArabaMothrider()));
        harness.setLibrary(player2, List.of(new ArabaMothrider()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, ashes.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ashes);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private void castAndChooseElf() {
        harness.castFromHand(player1, new AshesOfTheFallen(), "{2}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
    }
}
