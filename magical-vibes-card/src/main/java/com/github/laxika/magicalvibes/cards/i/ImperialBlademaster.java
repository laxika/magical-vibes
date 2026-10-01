package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "30")
public class ImperialBlademaster extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Adamant Will",
            "Akki Ronin",
            "Ancestral Katana",
            "Asari Captain",
            "Eater of Virtue",
            "Eiganjo Exemplar",
            "Eiganjo Uprising",
            "Heiko Yamazaki, the General",
            "Imperial Subduer",
            "Norika Yamazaki, the Poet",
            "Peerless Samurai",
            "Reinforced Ronin",
            "Selfless Samurai",
            "Sunblade Samurai",
            "Tempered in Solitude");

    public ImperialBlademaster() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SAMURAI),
                                new CardSubtypePredicate(CardSubtype.WARRIOR))),
                        new ConditionalEffect(new AttacksAlone(),
                                new DraftCardFromSpellbookEffect(SPELLBOOK))));
    }
}
