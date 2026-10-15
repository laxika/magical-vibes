package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExileIfLeavesBattlefieldUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "181")
public class ElementalExpressionist extends Card {

    public ElementalExpressionist() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        CreateTokenEffect elemental = new CreateTokenEffect(
                "Elemental", 4, 4, CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.ELEMENTAL));
        List<CardEffect> magecraft = List.of(
                new GrantExileIfLeavesBattlefieldUntilEndOfTurnEffect(),
                new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new SelfExiledFromBattlefieldEffect(elemental)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, magecraft, null, TargetFilters.creatureYouControl()));
        addEffect(EffectSlot.ON_CONTROLLER_COPIES_SPELL,
                new SpellCopyTriggerEffect(instantOrSorcery, magecraft, TargetFilters.creatureYouControl()));
    }
}
