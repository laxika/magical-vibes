package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "3")
public class DazzlingTheaterPropRoom extends Card {

    public DazzlingTheaterPropRoom() {
        setRoomDoorManaCosts(List.of("{3}{W}", "{2}{W}"));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Dazzling Theater", List.of())
                        .withManaCost("{3}{W}"),
                new ChooseOneEffect.ChooseOneOption("Prop Room", List.of())
                        .withManaCost("{2}{W}")
        )));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(0),
                new GrantSpellCastingAbilityToSpellsEffect(
                        Keyword.CONVOKE, new CardTypePredicate(CardType.CREATURE))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceRoomDoorUnlocked(1),
                new UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(
                        TurnStep.UNTAP, new PermanentIsCreaturePredicate(), TapUntapScope.CONTROLLED)));
    }
}
