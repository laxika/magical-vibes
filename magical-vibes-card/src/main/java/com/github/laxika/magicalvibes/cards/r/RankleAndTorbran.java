package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "252")
public class RankleAndTorbran extends Card {

    public RankleAndTorbran() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                new MayEffect(ChooseOneEffect.oneOrMore(List.of(
                        new ChooseOneEffect.ChooseOneOption("Each player creates a Treasure token.",
                                new EachPlayerCreatesTokenEffect(CreateTokenEffect.ofTreasureToken(1))),
                        new ChooseOneEffect.ChooseOneOption("Each player sacrifices a creature of their choice.",
                                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                                        SacrificeRecipient.EACH_PLAYER).withSimultaneousChoices()),
                        new ChooseOneEffect.ChooseOneOption(
                                "If a source would deal damage to a player or battle this turn, it deals that much damage plus 2 instead.",
                                new AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect(2))
                )), "Choose one or more modes?"));
    }
}
