package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithSourcePermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "83")
public class PlaneMergeElf extends Card {

    public PlaneMergeElf() {
        // Landship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it's a land, you may reveal it. If you do, create a 1/1 green Elf Warrior creature token.
        CreateTokenEffect elfWarrior = new CreateTokenEffect(
                "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ConditionalEffect.unless(
                new TopCardOfLibraryType(CardType.LAND),
                new MayEffect(
                        SequenceEffect.of(
                                new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                                elfWarrior),
                        "Reveal the land card?")));

        // Kinfall — Whenever a creature enters under your control, if it shares a creature type
        // with Plane-Merge Elf, creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentSharesCreatureTypeWithSourcePermanentPredicate(),
                        new BoostAllOwnCreaturesEffect(1, 1)));
    }
}
