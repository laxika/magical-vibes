package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

/**
 * Ready // Willing — a split card with fuse.
 * <p>
 * Ready {1}{G}{W}: Creatures you control gain indestructible until end of turn. Untap each creature
 * you control.
 * Willing {1}{W}{B}: Creatures you control gain deathtouch and lifelink until end of turn.
 * Fuse {2}{G}{W}{W}{B}: cast both halves as one spell, resolving Ready and then Willing (CR 702.102d).
 * <p>
 * The caster chooses which half (or both) before the spell goes on the stack (CR 709.3), so the
 * three choices are modelled as the modes of one {@link ChooseOneEffect}, each carrying its own
 * total mana cost — the fuse mode's is the two halves combined (CR 702.102c). Both halves are
 * untargeted mass grants.
 */
@CardRegistration(set = "DGM", collectorNumber = "132")
public class ReadyWilling extends Card {

    public ReadyWilling() {
        CardEffect indestructible = new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES);
        CardEffect untap = new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate());
        CardEffect deathtouchLifelink = new GrantKeywordEffect(
                Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.OWN_CREATURES);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Ready — Creatures you control gain indestructible until end of turn. Untap them",
                        List.of(indestructible, untap)
                ).withManaCost("{1}{G}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Willing — Creatures you control gain deathtouch and lifelink until end of turn",
                        deathtouchLifelink
                ).withManaCost("{1}{W}{B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Ready and then Willing",
                        List.of(indestructible, untap, deathtouchLifelink)
                ).withManaCost("{2}{G}{W}{W}{B}")
        )));
    }
}
